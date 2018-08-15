package com.hkq;

import java.util.Iterator;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/*
 * 就绪队列类，优先级[0, MAX_PRIORITY]
 */

public class ReadyQueue {
	private static int MAX_PRIORITY = 0;
	
	private int[] times;	// 每个优先级队列的时间片
	private ArrayList<Queue<PCB>> queueList = new ArrayList<>();	// 优先级队列
	
	/* 根据时间片创建就绪队列，最大优先级为times.length - 1 */
	public ReadyQueue(int[] times) {
		MAX_PRIORITY = times.length - 1;
		this.times = times;
		for(int i = 0; i <= MAX_PRIORITY; i++) {
			queueList.add(new LinkedList<>());
		}
	}
	
	/*
	 * 根据PCB中priority字段将进程加入到相应的队列的末尾
	 */
	public void addProcess(PCB process) {
		if(validatePriority(process.getPriority())) {
			queueList.get(process.getPriority()).add(process);
		} else {
			System.out.println("优先级不合法In ReadyQueue addProcess.");
		}
	}
	
	/*
	 * 获取该优先级队列的时间片
	 */
	public int getTimeSlice(int priority) {
		if(validatePriority(priority)) {
			return times[priority];
		}
		return 0;
	}
	
	public List<Queue<PCB>> getQueueList() {
		return this.queueList;
	}
	
	/*
	 * 调度程序，根据基于时间片的多队列反馈调度策略返回一个进程，若就绪队列为空，则返回nul
	 */
	public PCB getExecuteProcess() {
		Iterator<Queue<PCB>> iter = queueList.iterator();
		while(iter.hasNext()) {
			Queue<PCB> queue = iter.next();
			if(!queue.isEmpty()) {
				return queue.remove();
			}
		}
		return null;
	}
	
	/*
	 * 判断就绪队列是否为空
	 */
	public boolean isEmpty() {
		for(Queue<PCB> queue : queueList) {
			if(!queue.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * 判断该优先级是否合法
	 */
	public static boolean validatePriority(int priority) {
		return priority >= 0 && priority <= MAX_PRIORITY;
	}
	
	/*
	 * 获取最高优先级
	 */
	public static int getMaxPriority() {
		return MAX_PRIORITY;
	}
	
	/*
	 * 打印就绪队列
	 */
	public String print() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < queueList.size(); i++) {
			sb.append("Ready" + i + "\t" + queueList.get(i) + "\n");
		}
		return sb.toString();
	}
}
