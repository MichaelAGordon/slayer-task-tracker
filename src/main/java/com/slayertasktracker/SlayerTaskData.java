package com.slayertasktracker;

import java.util.LinkedHashMap;
import java.util.Map;

public class SlayerTaskData
{
	// Nested map to store tasks, date->(task_number->{details})
	private Map<String, LinkedHashMap<Integer, Map<String, String>>> dateToTasks;

	public SlayerTaskData()
	{
		this.dateToTasks = new LinkedHashMap<>();
	}

	public void addTaskForDate(String date, Map<String, String> taskDetails)
	{
		// Check if there are tasks for the given date
		LinkedHashMap<Integer, Map<String, String>> tasksForDate = dateToTasks.get(date);

		if (tasksForDate == null)
		{
			// No tasks for the given date, create a new LinkedHashMap and put it in dateToTasks
			tasksForDate = new LinkedHashMap<>();
			dateToTasks.put(date, tasksForDate);
		}

		// Find the next task number for the date
		int nextTaskNumber = tasksForDate.size() + 1;

		// Add the new task
		tasksForDate.put(nextTaskNumber, taskDetails);
	}

	// Getter for dateToTasks
	public Map<String, LinkedHashMap<Integer, Map<String, String>>> getDateToTasks()
	{
		return dateToTasks;
	}

	// Setter for dateToTasks
	public void setDateToTasks(Map<String, LinkedHashMap<Integer, Map<String, String>>> dateToTasks)
	{
		this.dateToTasks = dateToTasks;
	}
}
