## OSSchedule

模拟操作系统进程调度

### PCB类字段说明    

```java
/* 程序的基本信息，这些信息不变化 */
private String id;				// 标识符
private int startTime;			// 程序开始时间
private int totalTime;			// 总运行时间
private int blockStartTime;		// I/O开始时间，-1表示无阻塞
private int blockContinueTime; // I/O持续时间，如果blockTime = -1，则此字段无意义

/* OS修改这些信息来调度程序  */
private int state = NEW;				// 状态
private int priority = 0;			// 优先级
private int executeTime = 0;		// 程序当前已经运行的时间
private int alreadyBlockTime = 0;	// 如果处于阻塞状态，则记录已经阻塞的时间
```

### 调度整体流程

在 `Schedule.execute(time)`函数中体现了调度的整体流程：

* 模拟cpu运行time时间：①cpu运行时间加time；②正在执行的程序执行时间加time；③阻塞队列中所有进程已经阻塞时间加time。
* 将新建态队列中cpu运行时间大于等于进程开始时间的进程放入就绪队列0。
* 将阻塞队列中已经阻塞时间大于阻塞时间的进程，优先级减1，放入相应优先级的就绪队列。
* 决定正在运行进程的去向：
  * 结束：进程执行时间等于程序运行时间
  * 阻塞：将进程放入阻塞队列
  * 就绪：优先级减1，放入相应的就绪队列
* 从就绪队列中基于时间片的多队列反馈调度策略返回一个进程，设置其为运行态


```java
/**
 * 模拟cpu执行time时间
 */
private void execute(int time) {
	Iterator<PCB> iter;

	/* 1. 模拟cup运行time时间 */
	cpuTime += time;
	if(runningProcess != null) { // 程序运行时间增加
		runningProcess.executeTimeAdd(time);
	}
	iter = blockedList.iterator();
	while(iter.hasNext()) { // 阻塞时间增加
		iter.next().alreadyBlockedTimeAdd(time);
	}
	result.append("执行" + time + "，cpuTime " + cpuTime + "\n");
		
	/* 2. 将新建态中合适进程加入就绪态 */
	for(int i = 0; i < newList.size(); i++) {
		if(newList.get(i).isNewToReady(cpuTime)) {
			PCB process = newList.remove(i);
			process.setState(PCB.READY);
			process.setPriority(0);
			readyQueue.addProcess(process);
			i--;
			result.append("Process " + process.getId() + ": New -> Ready" + process.getPriority() + "\n");
		}
	}
		
	/* 3. 将阻塞队列中合适进程加入就绪态 */
	for(int i = 0; i < blockedList.size(); i++) {
		if(blockedList.get(i).isBlockedToReady()) {
			PCB process = blockedList.remove(i);
			process.setState(PCB.READY);
			process.priorityAdd1();
			readyQueue.addProcess(process);
			i--;
			result.append("Process " + process.getId() + ": Blocked -> Ready" + 	process.getPriority() + "\n");
		}
	}
		
	/* 4. 决定运行态进程去向 */
	if(runningProcess != null) {
		// 判断顺序必须为：是否结束、是否阻塞、是否就绪
		if(runningProcess.isExit()) {	
			runningProcess.setState(PCB.EXIT);
			exitList.add(runningProcess);
			result.append("Process " + runningProcess.getId() + ": Running -> Exit" + "\n");
		} else if(runningProcess.isBlocked()) {
					
			runningProcess.setState(PCB.BLOCKED);
			blockedList.add(runningProcess);
			result.append("Process " + runningProcess.getId() + ": Running -> Blocked" + "\n"	
		} else if(runningProcess.isRunningToReady()) {		
			runningProcess.setState(PCB.READY);
			runningProcess.priorityAdd1();
			readyQueue.addProcess(runningProcess);
			result.append("Process " + runningProcess.getId() + ": Running -> Ready" + runningProcess.getPriority() + "\n");
		}
		runningProcess = null;
	}
		
	/* 5. 从就绪队列中选择一个进程执行 */
	runningProcess = readyQueue.getExecuteProcess();
	if(runningProcess != null) {
		runningProcess.setState(PCB.RUNNING);
		result.append("Process " + runningProcess.getId() + ": Ready" + 	runningProcess.getPriority() + " -> Running" + "\n");
	} else {
		result.append(cpuTime + " - 就绪队列为空" + "\n");
	}
	result.append(printQueues());
	result.append("-----------------------------\n");
}
	
```

### 调度程序处理的时机

* 正在运行的程序发生阻塞、程序运行结束、或时间片用完。

* 当没有程序运行时，有进程从New->Ready、有进程从Blocked->Ready。

  因此，有一个**getNextExecutetime()****函数，**用于返回距离下一次调度需要执行的时间:

  * 当有程序正在运行：min{ 距离下一次阻塞时间、距离程序结束时间、该时间片用完 }
  * 当没有程序运行，则为min{阻塞队列中Blocked->Ready最短时间、新建态中New->Ready最短时间}

```java
private int getNextExecuteTime() {
	if(isShutdown()) {
		throw new RuntimeException("程序已经结束");
	} else if(runningProcess != null) {
		int time1 = runningProcess.getNowToBlockedStartTime();
		int time2 = runningProcess.getNowToExitTime();
		int time3 = readyQueue.getTimeSlice(runningProcess.getPriority());
		return Math.min(Math.min(time1, time2), time3);
	} else {
		// 阻塞队列中Blocked->Ready最短时间
		int minBlockedTime = Integer.MAX_VALUE;
		Iterator<PCB> iter = blockedList.iterator();
		while(iter.hasNext()) {
			PCB process = iter.next();
			int blockedTime = process.getNowToBlockedEndTime();
			if(blockedTime < minBlockedTime) {
				minBlockedTime = blockedTime;
			}
		}
		// 新建态中New->Ready最短时间
		int minStartTime = Integer.MAX_VALUE;
		iter = newList.iterator();
		while(iter.hasNext()) {
			PCB process = iter.next();
			int startTime = process.getNowToStartTime(cpuTime);
			if(startTime < minStartTime) {
				minStartTime = startTime;
			}
		}
		return Math.min(minBlockedTime, minStartTime);
	}
}
```

### 运行

在eclipse下导入项目,在Main类中运行即可。若想修改测试数据，在Main类main函数下，

* 修改times数组，修改就绪态时间片。
* 修改processes修改测试进程，输入进程Id、开始时间、运行时间、阻塞开始时间、阻塞结束时间，通过PCB构造方法创建一个进程。

[这里包含一个测试数据及结果](https://github.com/hkq-github/OSSchedule/blob/master/%E6%B5%8B%E8%AF%95.md)
