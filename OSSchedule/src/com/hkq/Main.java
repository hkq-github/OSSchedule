package com.hkq;

public class Main {
	public static void main(String[] args) {
		/* 优先级队列时间片 */
		int[] times = {10, 7, 4};
		
		PCB[] processes = {
				/* 进程Id、开始时间、运行时间、阻塞开始时间、阻塞结束时间 */
				new PCB("P1", 3, 23, 4, 9),
				new PCB("P2", 8, 17, -1, 0),
				new PCB("P3", 15, 10, 6, 2),
				new PCB("P4", 15, 9, 2, 4)
		};
		Schedule PC = new Schedule(times, processes);
		PC.start();
		System.out.println(PC.getResult());
	}
}
