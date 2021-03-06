package ph.com.sliconsulting.samples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates calendar schedules for project plans depending on duration and
 * task dependencies.
 * 
 * @author Bernard Andrei Pollo
 *
 */
public class ProjectScheduler {
	public class Task {
		private int id;
		private int[] dependencies;
		private int duration;
		private Calendar scheduleStart;
		private Calendar scheduleEnd;

		public Task(int id, int[] dependencies, int duration) {
			this.id = id;
			this.dependencies = dependencies;
			this.duration = duration;
		}

		@Override
		public String toString() {
			DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
			String scheduleStartStr = df.format(scheduleStart.getTime());
			String scheduleEndStr = df.format(scheduleEnd.getTime());
			return "Task " + id + ": duration:" + duration + " start:" + scheduleStartStr + " end:" + scheduleEndStr
					+ "";
		}
	}

	List<Task> generateSampleTasks(ProjectScheduler projectScheduler) {
		List<Task> tasks = new ArrayList<>();
		tasks.add(projectScheduler.new Task(1, null, 5));
		tasks.add(projectScheduler.new Task(2, new int[] { 1 }, 4));
		tasks.add(projectScheduler.new Task(3, new int[] { 1 }, 5));
		tasks.add(projectScheduler.new Task(4, new int[] { 3 }, 5));
		tasks.add(projectScheduler.new Task(5, new int[] { 3 }, 5));
		tasks.add(projectScheduler.new Task(6, new int[] { 5 }, 4));
		tasks.add(projectScheduler.new Task(7, new int[] { 1, 2 }, 5));
		return tasks;
	}

	Map<Integer, Task> taskMap;

	void generateTaskMap(List<Task> tasks) {
		taskMap = new HashMap<>();
		for (Task task : tasks) {
			if (taskMap.get(task.id) != null) {
				throw new RuntimeException("Duplicate Task ID detected: " + task.id);
			}
			taskMap.put(task.id, task);
		}
	}

	Calendar defaultStartDate;

	void generateDefaultStartDate() {
		defaultStartDate = Calendar.getInstance();
	}

	public List<Task> schedule(List<Task> tasks) {
		generateTaskMap(tasks);
		generateDefaultStartDate();

		for (Task task : tasks) {
			if (task.dependencies == null) {
				task.scheduleStart = (Calendar) defaultStartDate.clone();
				task.scheduleEnd = (Calendar) defaultStartDate.clone();
				task.scheduleEnd.add(Calendar.DATE, task.duration);
			} else {
				int durationWithDependencies = computeDurationWithDependencies(task);
				task.scheduleStart = (Calendar) defaultStartDate.clone();
				task.scheduleStart.add(Calendar.DATE, durationWithDependencies - task.duration);
				task.scheduleEnd = (Calendar) task.scheduleStart.clone();
				task.scheduleEnd.add(Calendar.DATE, task.duration);
			}
		}
		return tasks;
	}

	public int computeDurationWithDependencies(Task task) {
		if (task.dependencies == null) {
			return task.duration;
		} else {
			int[] dependencyDurations = new int[task.dependencies.length];
			for (int i = 0; i < task.dependencies.length; i++) {
				Task dependency = taskMap.get(task.dependencies[i]);
				dependencyDurations[i] = task.duration + computeDurationWithDependencies(dependency);
			}
			Arrays.sort(dependencyDurations);
			return (dependencyDurations[dependencyDurations.length - 1]);
		}
	}

	public static void main(String[] args) throws Exception {
		ProjectScheduler projectScheduler = new ProjectScheduler();
		List<Task> tasks = new ArrayList<>();
		System.out.println("Welcome to Project Scheduler.");
		while (true) {
			System.out.println("[1] Add a task");
			System.out.println("[2] Schedule!");
			System.out.println("[3] Reset tasks");
			System.out.println("[4] Exit");
			System.out.print("Choose option number:");
			String choice = new BufferedReader(new InputStreamReader(System.in)).readLine();
			switch (choice) {
			case "1":
				System.out.print("Input task ID number:");
				String taskIdStr = new BufferedReader(new InputStreamReader(System.in)).readLine();
				int taskId;
				try {
					taskId = Integer.parseInt(taskIdStr);
				} catch (NumberFormatException nfe) {
					System.out.println("Invalid input! Task was not added.");
					break;
				}
				projectScheduler.generateTaskMap(tasks);
				if (projectScheduler.taskMap.get(taskId) != null) {
					System.out.println("Duplicate task ID found! Task was not added.");
					break;
				}

				System.out.println("Input dependency ID numbers separated by comma.");
				System.out.println("Or do not input any number and press enter if no dependency.");
				System.out.println("E.g.: 1,3,7");
				System.out.print("Dependency ID numbers:");
				String dependencyIdsStr = new BufferedReader(new InputStreamReader(System.in)).readLine();
				int[] dependencyIds;
				if ("".equals(dependencyIdsStr)) {
					dependencyIds = null;
				} else {
					String[] dependencyIdsStrArr = dependencyIdsStr.trim().split(",");
					dependencyIds = new int[dependencyIdsStrArr.length];
					try {
						for (int i = 0; i < dependencyIdsStrArr.length; i++) {
							dependencyIds[i] = Integer.parseInt(dependencyIdsStrArr[i]);
						}

						boolean taskDependencyOnItself = false;
						for (int i = 0; i < dependencyIdsStrArr.length; i++) {
							if (dependencyIds[i] == taskId) {
								taskDependencyOnItself = true;
								break;
							}
						}
						if (taskDependencyOnItself) {
							System.out.println("Task cannot depend on itself! Task was not added.");
							break;
						}

						boolean unknownDependency = false;
						projectScheduler.generateTaskMap(tasks);
						for (int i = 0; i < dependencyIdsStrArr.length; i++) {
							if (projectScheduler.taskMap.get(dependencyIds[i]) == null) {
								unknownDependency = true;
								break;
							}
						}
						if (unknownDependency) {
							System.out.println("Unknown task ID dependency detected! Task was not added.");
							break;
						}

					} catch (NumberFormatException nfe) {
						System.out.println("Invalid input! Task was not added.");
						break;
					}
				}

				System.out.println("Input task duration in days.");
				System.out.println("Whole numbers only.");
				System.out.print("Task duration:");
				String durationStr = new BufferedReader(new InputStreamReader(System.in)).readLine();
				int duration;
				try {
					duration = Integer.parseInt(durationStr);
				} catch (NumberFormatException nfe) {
					System.out.println("Invalid input! Task was not added.");
					break;
				}
				tasks.add(projectScheduler.new Task(taskId, dependencyIds, duration));
				System.out.println("Task " + taskId + " has been added.");
				break;
			case "2":
				if (tasks.isEmpty()) {
					System.out.println("It seems that you have not added tasks yet.");
					System.out.println("Add tasks first.");
				} else {
					System.out.println("Here's your schedule:");
					tasks = projectScheduler.schedule(tasks);
					for (Task task : tasks) {
						System.out.println(task);
					}
				}
				break;
			case "3":
				tasks = new ArrayList<>();
				System.out.println("Tasks have been reset.");
				break;
			case "4":
				System.out.print("Bye!");
				return;
			default:
				System.out.println("Invalid option. Choose again.");
			}
		}
	}
}
