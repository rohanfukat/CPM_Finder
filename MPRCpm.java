
package com.mycompany.graphtutorial;
/**
 *
 * @author Group 28
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import java.util.ArrayList;
import java.util.List;

class Task {
    String name;
    int duration;
    int earlyStart;
    int earlyFinish;
    int lateStart;
    int lateFinish;
    int slack;
    boolean isCritical;
    List<Task> dependencies;

    public Task(String name, int duration) {
        this.name = name;
        this.duration = duration;
        this.dependencies = new ArrayList<>();
        this.isCritical = false;
    }

    public void addDependency(Task dependency) {
        this.dependencies.add(dependency);
    }
}


public class MPRCpm extends JFrame {
    JTextField numTasksField;
    JButton submitButton;
    JTextField[] taskNameField;
    JTextField[] dependencyFields;
    JTextField[] durationFields;

    public MPRCpm() {
        setTitle("CPM Tasks Input");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JLabel numTasksLabel = new JLabel("Number of Tasks:");
        numTasksField = new JTextField(5);
        inputPanel.add(numTasksLabel);
        inputPanel.add(numTasksField);

        add(inputPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        submitButton = new JButton("Submit");
        submitButton.addActionListener(new SubmitButtonListener());

        buttonPanel.add(submitButton);

        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int numTasks = Integer.parseInt(numTasksField.getText());
            
            JPanel taskDetailsPanel = new JPanel();

            taskNameField = new JTextField[numTasks];
            durationFields = new JTextField[numTasks];
            dependencyFields = new JTextField[numTasks];

            for (int i = 0; i < numTasks; i++) {
                JLabel taskLabel = new JLabel("Task " + (i + 1) + " Name:");
                taskNameField[i] = new JTextField();
                add(taskLabel);
                add(taskNameField[i]);

                JLabel durationLabel = new JLabel("Duration:");
                durationFields[i] = new JTextField();
                add(durationLabel);
                add(durationFields[i]);
                setLayout(new GridLayout(0, 2));

                JLabel dependencyLabel = new JLabel("Dependencies (comma-separated):");
                dependencyFields[i] = new JTextField();
                add(dependencyLabel);
                add(dependencyFields[i]);
            }
  
            
             // Use JScrollPane to add a scrollbar to the task details panel
//                JScrollPane scrollPane = new JScrollPane(taskDetailsPanel);
//                add(scrollPane, BorderLayout.CENTER);

            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener(new CalculateButtonListener());
            add(calculateButton, BorderLayout.SOUTH);
            revalidate();
            repaint();
            pack();
            submitButton.setEnabled(false);
            numTasksField.setEnabled(false);
            
        }
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Graph graph = new SingleGraph("Tutorial 1");
            List<Task> tasks = new ArrayList<>();
            System.setProperty("org.graphstream.ui", "swing");

            for (int i = 0; i < taskNameField.length; i++) {
                String taskName = taskNameField[i].getText();
                graph.addNode(taskName);
                graph.getNode(taskName).setAttribute("ui.label", taskName);
                graph.getNode(taskName).setAttribute("ui.style", "shape: box; size: 70px, 40px; fill-mode: plain; fill-color: white; stroke-mode: plain; stroke-color: blue; text-size:20px;");
                int taskDuration = Integer.parseInt(durationFields[i].getText());
                String[] dependencyNames = dependencyFields[i].getText().split(",");
//                List<Task> dependencies = new ArrayList<>();
                
                Task task = new Task(taskName, taskDuration);
                tasks.add(task);


                for (String dependencyName : dependencyNames) {
                    if(dependencyName.trim().isEmpty())
                    {break;}
                    String trimmedDependencyName = dependencyName.trim();
                    
                    Task dependencyTask = tasks.stream().filter(t -> t.name.equals(trimmedDependencyName)).findFirst()
                            .orElse(null);
                    if (dependencyTask != null) {
                        task.addDependency(dependencyTask);
                        System.out.println(task.name+dependencyTask.name+" "+ dependencyTask.name+" "+ task.name);
                        graph.addEdge(task.name+dependencyTask.name, dependencyTask.name, task.name);

                    } else {
                        JOptionPane.showMessageDialog(MPRCpm.this, "Dependency not found for task " + taskName
                                + ". Please make sure the dependency task exists.");
                        return;
                    }
                }
            }
            
            graph.display();

            calculateCPM(tasks, graph);
              System.out.println("\n\nTask\tDuration\tEarly Start\tEarly Finish\tLate Start\tLate Finish\tSlack");
        for (Task task : tasks) {
            System.out.println(task.name + "\t" + task.duration + "\t\t" + task.earlyStart + "\t\t" +
                    task.earlyFinish + "\t\t" + task.lateStart + "\t\t" + task.lateFinish + "\t\t" + task.slack );
        }

            JFrame resultFrame = new JFrame("CPM Results");
            resultFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            resultFrame.setSize(600, 400);
            resultFrame.setLayout(new BorderLayout());

            JTextArea resultTextArea = new JTextArea();
            resultTextArea.setEditable(false);
            resultTextArea.append("Task\tDuration\tEarly Start\tEarly Finish\tLate Start\tLate Finish\tSlack\n");
            for (Task task : tasks) {
                resultTextArea.append(task.name + "\t" + task.duration + "\t" + task.earlyStart + "\t" +
                        task.earlyFinish + "\t" + task.lateStart + "\t" + task.lateFinish + "\t" + task.slack
                        + "\n");
            }

            resultFrame.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
            resultFrame.setVisible(true);
        }
    }

    // Method for Forward and backward Pass
    public static void calculateCPM(List<Task> tasks, Graph graph) {
        int currentTime = 0;

        // Early Start of First Task must be 0
        tasks.get(0).earlyStart = 0;
        tasks.get(0).earlyFinish = tasks.get(0).duration;

        // Forward pass
        for (int i = 1; i < tasks.size(); i++) {
            Task currentTask = tasks.get(i);
            currentTask.earlyStart = currentTime;
            currentTask.earlyFinish = currentTime + currentTask.duration;

            // Find the latest early finish time among dependencies
            int maxDependencyFinishTime = 0;

            for (Task dependency : currentTask.dependencies) {
                maxDependencyFinishTime = Math.max(maxDependencyFinishTime, dependency.earlyFinish);
            }

            // Updating early start and early finish
            currentTask.earlyStart = maxDependencyFinishTime;
            currentTask.earlyFinish = maxDependencyFinishTime + currentTask.duration;
        }

        // Getting EF of the last task from the list
        int projectDuration = tasks.get(tasks.size() - 1).earlyFinish;

        // Backward pass
        for (int i = tasks.size() - 1; i >= 0; i--) {
            Task currentTask = tasks.get(i);

            // For the last task of the project
            if (i == tasks.size() - 1) {
                currentTask.lateFinish = projectDuration;
                currentTask.lateStart = projectDuration - currentTask.duration;
            } else {
                for (Task nTask : tasks) {
                    int TaskNewCurrentTime = 0;
                    int taskInitialCurrentTime = 0;
                    
                    if (nTask.dependencies.contains(currentTask)) {
                        if (currentTask.lateFinish == 0) {        
                            currentTask.lateFinish = nTask.lateStart;
                           currentTask.lateStart = currentTask.lateFinish - currentTask.duration;
                           System.out.println("hello");
                           System.out.println(currentTask.lateFinish);
                        } else {

                               TaskNewCurrentTime = nTask.lateStart;
                               taskInitialCurrentTime = Math.min(currentTask.lateFinish,TaskNewCurrentTime);
                               System.out.println("\n\nTask Details : "+taskInitialCurrentTime+"\n\n");
                               System.out.println("Current Task Duration : "+ currentTask.duration);
                               currentTask.lateFinish = taskInitialCurrentTime;
                               currentTask.lateStart = currentTask.lateFinish - currentTask.duration;
                        }
                    }
                }
            }

            // Calculating slack Formula
            currentTask.slack = currentTask.lateFinish - currentTask.earlyFinish;
            if (currentTask.slack == 0) {
                currentTask.isCritical = true;
            }
        }

        System.out.println("\n\nCritical Path : \n");
        for (Task displayTask : tasks) {
            if (displayTask.isCritical) {
                System.out.print(displayTask.name + " -> ");
                                graph.getNode(displayTask.name).setAttribute("ui.style", " fill-color: red;");

            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MPRCpm());
    }
}

