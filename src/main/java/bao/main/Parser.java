package bao.main;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import bao.task.Deadline;
import bao.task.Event;
import bao.task.Task;
import bao.task.ToDo;

/**
 * The Parser class is responsible for interpreting user input and executing the needed actions, such as adding,
 * listing, marking and un-marking or deleting tasks.
 */
public class Parser {

    /**
     * Parses the user command and executes the appropriate action.
     *
     * @param command Full user command string.
     * @param tasks TaskList object containing all current tasks.
     * @param ui Ui object for user display and interaction.
     * @param storage Storage object for saving and loading tasks.
     */
    public static void parse(String command, TaskList tasks, Ui ui, Storage storage) {
        String[] parts = command.split(" ", 2);
        String commandType = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        switch (commandType) {
        case "bye" -> {
            ui.showExitMessage();
            System.exit(0);
        }
        case "list" -> {
            if (tasks.getTasks().isEmpty()) {
                ui.showMessage("Bao is not tracking anything!");
                break;
            }
            for (int i = 0; i < tasks.size(); i++) {
                ui.showMessage((i + 1) + ". " + tasks.getTask(i).toString());
            }
        }
        case "mark" -> {
            try {
                int index = Integer.parseInt(args) - 1;
                tasks.getTask(index).mark();
                storage.save(tasks.getTasks());
                ui.showMessage("Bao has marked it as done!");
                ui.showMessage(tasks.getTask(index).toString());
            } catch (Exception e) {
                ui.showMessage("Bao needs a valid task number to mark!");
            }
        }
        case "unmark" -> {
            try {
                int index = Integer.parseInt(args) - 1;
                tasks.getTask(index).unmark();
                storage.save(tasks.getTasks());
                ui.showMessage("Bao has marked it as not done!");
                ui.showMessage(tasks.getTask(index).toString());
            } catch (Exception e) {
                ui.showMessage("Bao needs a valid task number to unmark!");
            }
        }
        case "todo" -> {
            if (args.isEmpty()) {
                ui.showMessage("Bao needs a description of the task!");
            } else {
                try {
                    tasks.addTask(new ToDo(args));
                    storage.save(tasks.getTasks());
                    ui.showMessage("Bao got it! Bao is now tracking:");
                    ui.showMessage(tasks.getTask(tasks.size() - 1).toString());
                } catch (IOException e) {
                    ui.showMessage("Bao could not save tasks");
                }
            }
        }
        case "deadline" -> {
            String[] argParts = args.split(" /by ");
            if (argParts.length < 2) {
                ui.showMessage("Bao needs a proper description and deadline for the task!");
            } else {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(argParts[1], Bao.getInputDateFormat());
                    tasks.addTask(new Deadline(argParts[0], dateTime));
                    storage.save(tasks.getTasks());
                    ui.showMessage("Bao got it! Bao is now tracking:");
                    ui.showMessage(tasks.getTask(tasks.size() - 1).toString());
                } catch (DateTimeParseException e) {
                    ui.showMessage("Bao needs a valid date format");
                } catch (IOException e) {
                    ui.showMessage("Bao could not save tasks");
                }
            }
        }
        case "event" -> {
            String[] argParts = args.split(" /from | /to ");
            if (argParts.length < 3) {
                ui.showMessage("Bao needs a proper description and duration for the task!");
            } else {
                try {
                    LocalDateTime from = LocalDateTime.parse(argParts[1], Bao.getInputDateFormat());
                    LocalDateTime to = LocalDateTime.parse(argParts[2], Bao.getInputDateFormat());
                    tasks.addTask(new Event(argParts[0], from, to));
                    storage.save(tasks.getTasks());
                    ui.showMessage("Bao got it! Bao is now tracking:");
                    ui.showMessage(tasks.getTask(tasks.size() - 1).toString());
                } catch (DateTimeParseException e) {
                    ui.showMessage("Bao needs a valid date format");
                } catch (IOException e) {
                    ui.showMessage("Bao could not save tasks");
                }
            }
        }
        case "delete" -> {
            try {
                int index = Integer.parseInt(args) - 1;
                Task removed = tasks.getTask(index);
                tasks.deleteTask(index);
                storage.save(tasks.getTasks());
                ui.showMessage("Bao has removed this task:");
                ui.showMessage(removed.toString());
                ui.showMessage("Bao is now tracking " + tasks.size() + " tasks");
            } catch (Exception e) {
                ui.showMessage("Bao needs a task number to delete!");
            }
        }
        case "on" -> {
            try {
                LocalDate date = LocalDate.parse(args, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                ui.showMessage("Bao showing tasks on " + date.format(Bao.getDateOnlyFormat()) + ":");
                boolean found = false;
                for (Task task : tasks.getTasks()) {
                    if (task instanceof Deadline) {
                        Deadline deadlineTask = (Deadline) task;
                        if (deadlineTask.getDate().toLocalDate().equals(date)) {
                            ui.showMessage(deadlineTask.toString());
                            found = true;
                        }
                    } else if (task instanceof Event) {
                        Event eventTask = (Event) task;
                        if (eventTask.getFromDateTime().toLocalDate().equals(date)) {
                            ui.showMessage(eventTask.toString());
                            found = true;
                        }
                    }
                    if (!found) {
                        ui.showMessage("Bao cannot find any tasks on this date!");
                    }
                }

            } catch (DateTimeParseException e) {
                ui.showMessage("Bao needs a valid date format such as 2024-08-28");
            }
        }
        case "find" -> {
            if (args.isEmpty()) {
                ui.showMessage("Bao needs a keyword to find in the tasks!");
            } else {
                ArrayList<Task> foundTasks = tasks.findTasks(args.trim());
                if (foundTasks.isEmpty()) {
                    ui.showMessage("Bao could not find any tasks with the keyword");
                } else {
                    ui.showMessage("Bao found these tasks with the keyword!");
                    for (int i = 0; i < foundTasks.size(); i++) {
                        ui.showMessage((i + 1) + ". " + foundTasks.get(i).toString());
                    }
                }
            }
        }
        default -> {
            ui.showMessage("Bao needs a proper command :(");
        }
        }
    }
}
