package com.blogger.rajesh.demo;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TaskMonitor extends Application {

	@Override
	public void start(Stage primaryStage) {
		final IntegerProperty tasksCreated = new SimpleIntegerProperty(0);

		final ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		};
		final ServiceExecutor exec = new ServiceExecutor(Executors.newCachedThreadPool());

		final TableView<Worker<?>> taskTable = createTable();
		taskTable.setItems(exec.getWorkerList());

		final Button newTaskButton = new Button();
		newTaskButton.textProperty().bind(Bindings.format("Create task %d", tasksCreated.add(1)));
		newTaskButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				tasksCreated.set(tasksCreated.get() + 1);
				CountingService service=new CountingService("Task"+ tasksCreated.get());
				service.setExecutor(exec);
				service.restart();
				
			}
		});

		final BorderPane root = new BorderPane();
		root.setCenter(taskTable);
		final HBox controls = new HBox();
		controls.setPadding(new Insets(10));
		controls.setAlignment(Pos.CENTER);
		controls.getChildren().add(newTaskButton);
		root.setBottom(controls);

		final Scene scene = new Scene(root, 600, 400);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private TableView<Worker<?>> createTable() {
		final TableView<Worker<?>> taskTable = new TableView<>();
		final TableColumn<Worker<?>, String> titleCol = new TableColumn<>("Title");
		titleCol.setCellValueFactory(new PropertyValueFactory<Worker<?>, String>("title"));
		final TableColumn<Worker<?>, Double> progressCol = new TableColumn<>("Progress");
		progressCol.setCellValueFactory(new PropertyValueFactory<Worker<?>, Double>("progress"));
		progressCol.setCellFactory(new Callback<TableColumn<Worker<?>, Double>, TableCell<Worker<?>, Double>>() {

			@Override
			public TableCell<Worker<?>, Double> call(TableColumn<Worker<?>, Double> col) {
				return new ProgressTabelCell();
			}

		});
		final TableColumn<Worker<?>, State> stateCol = new TableColumn<>("State");
		stateCol.setCellValueFactory(new PropertyValueFactory<Worker<?>, State>("state"));
		final TableColumn<Worker<?>, String> messageCol = new TableColumn<>("Message");
		messageCol.setCellValueFactory(new PropertyValueFactory<Worker<?>, String>("message"));
		messageCol.setPrefWidth(200);
		taskTable.getColumns().addAll(Arrays.asList(titleCol, progressCol, stateCol, messageCol));
		return taskTable;
	}

	private static class CountingTask extends Task<Void> {

		private CountingTask(String title) {
			updateTitle(title);
		}

		@Override
		protected Void call() throws Exception {
			final int n = new Random().nextInt(100) + 100;
			for (int i = 0; i < n; i++) {
				updateProgress(i, n);
				updateMessage(String.format("Count is %d (of %d)", i, n));
				Thread.sleep(100);
			}
			return null;
		}
	}

	private static class CountingService extends Service<Void> {

		private String title;

		public CountingService(String title) {
			this.title = title;
		}

		@Override
		protected void executeTask(Task<Void> task) {
			Executor executor = getExecutor();
			executor.execute(task);
		}

		@Override
		protected Task<Void> createTask() {
			// TODO Auto-generated method stub
			return new CountingTask(title);
		}

	}

	private static class ProgressTabelCell extends TableCell<Worker<?>, Double> {
		final ProgressBar progressBar = new ProgressBar();

		@Override
		public void updateItem(Double value, boolean empty) {
			if (empty || value == null) {
				setGraphic(null);
			} else {
				setGraphic(progressBar);
				progressBar.setProgress(value);
			}
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}