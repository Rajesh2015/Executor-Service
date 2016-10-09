package com.blogger.rajesh.demo;
import java.util.concurrent.Executor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;


/**
 * Wraps an Executor and exposes an ObservableList of Workers which have been
 * executed but have not completed. (Workers are considered completed if they 
 * exit a RUNNING state; i.e. they are in a SUCCEEDED, FAILED, or CANCELLED state.)
 *
 */
public class ServiceExecutor implements Executor {
    private final Executor exec ;
    private final ObservableList<Worker<?>> taskList ;
    public ServiceExecutor(Executor exec) {
        this.exec = exec;
        this.taskList = FXCollections.observableArrayList();
    }
    @Override
    public void execute(Runnable command) {
        if (command instanceof Worker) {
            final Worker<?> task = (Worker<?>) command ;
            task.stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue<? extends State> obs,
                        State oldState, State newState) {
                    if (oldState == State.RUNNING) {
                        taskList.remove(task);
                    }
                }
            });
            taskList.add(task);
        }
        exec.execute(command);
    }
    public ObservableList<Worker<?>> getWorkerList() {
        return taskList;
    }

}