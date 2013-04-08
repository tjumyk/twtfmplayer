package org.tjumyk.thread;

import java.util.ArrayList;

public abstract class WorkFlowRunner extends BackgroundRunner {

	public static boolean keepRunning = true;
	public static void cancelNewWorks(){
		keepRunning = false;
	};
	
	protected Work currentWork = null;
	protected ArrayList<Work> workList = new ArrayList<Work>();

	public abstract void initWorkFlow();

	@Override
	public void background() throws Exception {
		initWorkFlow();
		for (Work work : workList) {
			if(!keepRunning)
				break;
			currentWork = work;
			if (work instanceof ForeWork){
				System.out.println("Start ForeWork: "+work.getDescription());
				runForeground();
			}else{
				System.out.println("Start BackWork: "+work.getDescription());
				work.start();
			}
		}
	}

	@Override
	public void foreground() throws Exception {
		currentWork.start();
	}
	
	public void addWorks(Work... works){
		for(Work work : works)
			workList.add(work);
	}

	public static abstract class Work {
		String description;
		public Work(){
			this.description = "No description";
		}
		public Work(String description){
			this.description = description;
		}

		public abstract void start() throws Exception;
		public String getDescription(){
			return description;
		}
	}

	public static abstract class BackWork extends Work {
		public BackWork(){}
		public BackWork(String description){
			super(description);
		}
	}

	public static abstract class ForeWork extends Work {
		public ForeWork(){}
		public ForeWork(String description){
			super(description);
		}
	}
}
