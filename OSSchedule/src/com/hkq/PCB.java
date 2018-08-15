package com.hkq;

/*
 * PCB类
 */

public class PCB {
	
	/* 进程运行的5中状态 */
	public static final int NEW = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int BLOCKED = 3;
	public static final int EXIT = 4;
	
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
	
	private String otherMess = "PCB其他信息，如存储地址、现成信息等等";		// 一些其他信息，如存储地址、现成信息等

	public PCB(String id, int startTime, int totalTime, int blockStartTime, int blockContinueTime) {
		super();
		this.id = id;
		this.startTime = startTime;
		this.totalTime = totalTime;
		this.blockStartTime = blockStartTime;
		this.blockContinueTime = blockContinueTime;
	}
	
	public int getTotalTime() {
		return totalTime;
	}


	public int getBlockStartTime() {
		return blockStartTime;
	}


	public int getBlockContinueTime() {
		return blockContinueTime;
	}


	public int getExecuteTime() {
		return executeTime;
	}


	public int getAlreadyBlockTime() {
		return alreadyBlockTime;
	}


	public int getStartTime() {
		return startTime;
	}
	
	public String getId() {
		return id;
	}
	
	public int getState() {
		return state;
	}
	/*
	 * 设置当前程序状态
	 */
	public void setState(int state) {
		if(state >= PCB.NEW && state <= PCB.EXIT) {
			this.state = state;
		}
	}
	
	/*
	 * 优先级加1，若处于最高优先级，则什么也不做
	 */
	public void priorityAdd1() {
		if(this.priority < ReadyQueue.getMaxPriority()) {
			this.priority += 1;
		}
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		if(ReadyQueue.validatePriority(priority)) {
			this.priority = priority;
		}
	}

	/*
	 * 程序执行时间加time
	 */
	public void executeTimeAdd(int time) {
		this.executeTime += time;
	}
	
	/*
	 * 程序阻塞时间加time
	 */
	public void alreadyBlockedTimeAdd(int time) {
		this.alreadyBlockTime += time;
	}
	
	/*
	 * RUNNING时判断是否应该结束
	 */
	public boolean isExit() {
		if(state == RUNNING) {
			return executeTime >= totalTime;
		}
		throw new RuntimeException("错误的使用PCB.isEXIT函数。");
	}
	
	/*
	 * RUNNING时判断是否应该阻塞
	 */
	public boolean isBlocked() {
		if(state == RUNNING) {
			return executeTime == blockStartTime;
		}
		throw new RuntimeException("错误的使用PCB.isBlocked函数。");
	}
	
	/*
	 * RUNNING时是否应该放入就绪态
	 */
	public boolean isRunningToReady() {
		if(state == RUNNING) {
			return executeTime < totalTime;
		}
		throw new RuntimeException("错误的使用PCB.isRunningToReady函数。");
	}
	
	/*
	 * BLOCKED时判断是否应该就绪
	 */
	public boolean isBlockedToReady() {
		if(state == BLOCKED) {
			return alreadyBlockTime >= blockContinueTime;
		}
		throw new RuntimeException("错误的使用PCB.isBlockedToReady函数。");
	}
	
	/*
	 * NEW时判断是否应该就绪
	 */
	public boolean isNewToReady(int cpuTime) {
		if(state == NEW) {
			return cpuTime >= startTime;
		}
		throw new RuntimeException("错误的使用isNewToReady函数");
	}
	
	/*
	 * 返回进程距离New到Ready的时间，程序必须处于New，否则返回Integer.MAX_VALUE
	 */
	public int getNowToStartTime(int cpuTime) {
		if(state != PCB.NEW) {
			return Integer.MAX_VALUE;
		}
		if(cpuTime >= startTime) {
			return 0;
		} else {
			return startTime - cpuTime;
		}
	}
	
	/*
	 * 返回进程距离结束的时间，程序必须处于RUNNING，否则返回Integer.MAX_VALUE
	 */
	public int getNowToExitTime() {
		if(state != PCB.RUNNING || executeTime >= totalTime) {
			return Integer.MAX_VALUE;
		}
		return totalTime - executeTime;
	}
	
	/*
	 * 返回进程距离阻塞结束时间，若该进程不阻塞或已经阻塞完成或未处于阻塞态，则返回Integer.MAX_VALUE
	 */
	public int getNowToBlockedEndTime() {
		if(state != PCB.BLOCKED || blockStartTime == -1 || alreadyBlockTime >= blockContinueTime) {
			return Integer.MAX_VALUE;
		}
		return blockContinueTime - alreadyBlockTime;
	}
	
	/*
	 * 返回进程距离阻塞开始的时间，若该进程不阻塞或已经阻塞完成或不处于运行态，则返回Integer.MAX_VALUE
	 */
	public int getNowToBlockedStartTime() {
		if(state != PCB.RUNNING || blockStartTime == -1 || executeTime >= blockStartTime) {
			return Integer.MAX_VALUE;
		}
		return blockStartTime - executeTime;
	}
	
	@Override
	public String toString() {
		return id + "(" + executeTime + ", " + alreadyBlockTime + ")";
	}
}
