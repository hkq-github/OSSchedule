package com.hkq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class Schedule {
	
	private ArrayList<PCB> newList = new ArrayList<>();
	private ReadyQueue readyQueue;
	private PCB runningProcess;
	private ArrayList<PCB> blockedList = new ArrayList<>();
	private ArrayList<PCB> exitList = new ArrayList<>();
	
	private int cpuTime = 0;
	
	StringBuilder result = new StringBuilder();
	
	public Schedule(int[] times, PCB[] processes) {
		readyQueue = new ReadyQueue(times);
		// 根据开始时间对processes排序
		Arrays.sort(processes, new Comparator<PCB>() {
			@Override
			public int compare(PCB o1, PCB o2) {
				return o1.getStartTime() - o2.getStartTime();
			}
		});
		newList.addAll(Arrays.asList(processes));
		result.append("cpuTime 0\n");
		result.append(printQueues());
		result.append("-----------------------------\n");
	}
	
	public void start() {
		while(execute() != true) {
			;
		}
	}
	
	/*
	 * 执行下一次调度，返回是否应该shutdown
	 */
	public boolean execute() {
		if(!isShutdown()) {
			execute(getNextExecuteTime());
			return false;
		} 
		result.append("New, Ready, blocked, Running队列都为空，程序运行结束");
		return true;
	}
	
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
				result.append("Process " + process.getId() + ": Blocked -> Ready" + process.getPriority() + "\n");
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
				result.append("Process " + runningProcess.getId() + ": Running -> Blocked" + "\n");
				
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
			result.append("Process " + runningProcess.getId() + ": Ready" + runningProcess.getPriority() + " -> Running" + "\n");
		} else {
			result.append(cpuTime + " - 就绪队列为空" + "\n");
		}
		result.append(printQueues());
		result.append("-----------------------------\n");
	}
	
	/*
	 * 获取下一次执行时间：
	 * 1. 当有程序正在运行：min{距离下一次阻塞时间、距离程序结束时间、该时间片用完}
	 * 2. 当没有程序运行，则为min{阻塞队列中Blocked->Ready最短时间、新建态中New->Ready最短时间}
	 */
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
	
	/*
	 * 判断是否结束，如果新建态、就绪态、阻塞态、运行态都空，返回真
	 */
	public boolean isShutdown() {
		return newList.isEmpty() && readyQueue.isEmpty() && blockedList.isEmpty() && runningProcess == null;
	}	
	
	/*
	 * 打印队列
	 */
	public String printQueues() {
		StringBuilder sb = new StringBuilder();
		result.append("New\t" + newList + "\n");
		result.append("Blocked\t" + blockedList + "\n");
		result.append(readyQueue.print());
		result.append("Running\t" + runningProcess + "\n");
		result.append("Exit\t" + exitList + "\n");
		return sb.toString();
	}
	
	/*
	 * getter 方法
	 */
	public String getResult() {
		return result.toString();
	}

	public ArrayList<PCB> getNewList() {
		return newList;
	}

	public ReadyQueue getReadyQueue() {
		return readyQueue;
	}

	public PCB getRunningProcess() {
		return runningProcess;
	}

	public ArrayList<PCB> getBlockedList() {
		return blockedList;
	}

	public ArrayList<PCB> getExitList() {
		return exitList;
	}

	public int getCpuTime() {
		return cpuTime;
	}
}
