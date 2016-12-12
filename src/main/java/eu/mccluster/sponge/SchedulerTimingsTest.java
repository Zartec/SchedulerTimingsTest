package eu.mccluster.sponge;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import com.google.inject.Inject;

import co.aikar.timings.Timings;

@Plugin(id = "schedulertimingstest", name = "SchedulerTimingsTest", version = "1.0-SNAPSHOT")
public class SchedulerTimingsTest {
	@Inject
	private Logger logger;
	
	private Random rand;
	
	private Freeze freeze;
	
	@Listener
    public void onServerStart(GameStartedServerEvent event) {
		rand = new Random();
		freeze = new Freeze();
		logger.warn("Starting task with 10 seconds delay");
		Sponge.getScheduler().createTaskBuilder().delay(10, TimeUnit.SECONDS).execute(() -> {
			logger.warn("Executed task with 10 seconds delay");
			async();
		}).submit(this);
		Sponge.getScheduler().createTaskBuilder().delay(4, TimeUnit.MINUTES).execute(this::stop).submit(this);
    }
	
	private void async() {
		int delay = rand.nextInt(19) + 1;
		logger.warn("Starting async task with " + delay + " seconds delay");
		Sponge.getScheduler().createTaskBuilder().async().delay(delay, TimeUnit.SECONDS).execute(() -> {
			logger.warn("Executed async task with " + delay + " seconds delay");
			Sponge.getScheduler().createTaskBuilder().execute(delay % 2 == 0 ? this::freeze : freeze).submit(this);
		}).submit(this);
	}
	
	class Freeze implements Runnable {

		@Override
		public void run() {
			int delay = rand.nextInt(19) + 1;
			logger.warn("Freezing for " + delay + " seconds");
			try {
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
			}
			logger.warn("Freezed for " + delay + " seconds");
			async();
		}
		
	}
	
	private void freeze() {
		int delay = rand.nextInt(19) + 1;
		logger.warn("Freezing for " + delay + " seconds");
		try {
			Thread.sleep(delay * 1000);
		} catch (InterruptedException e) {
		}
		logger.warn("Freezed for " + delay + " seconds");
		async();
	}
	
	private void stop() {
		Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
		Timings.generateReport(Sponge.getServer().getConsole());
		Sponge.getScheduler().createTaskBuilder().delay(10, TimeUnit.SECONDS).execute(() -> {
			Sponge.getServer().shutdown();
		}).submit(this);
	}
}
