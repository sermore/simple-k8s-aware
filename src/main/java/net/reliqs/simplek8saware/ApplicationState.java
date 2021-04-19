package net.reliqs.simplek8saware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApplicationState {
    private static final Logger log = LoggerFactory.getLogger(ApplicationState.class);

    private final ApplicationContext applicationContext;
    private final ApplicationAvailability availability;
    @Value("${msg:no message}")
    private String msg;

    public ApplicationState(ApplicationContext applicationContext, ApplicationAvailability availability) {
        this.applicationContext = applicationContext;
        this.availability = availability;
    }

    @Scheduled(cron = "${state.readiness.cron:-}")
    public void readiness() {
        ReadinessState newState = availability.getReadinessState() == ReadinessState.ACCEPTING_TRAFFIC ? ReadinessState.REFUSING_TRAFFIC : ReadinessState.ACCEPTING_TRAFFIC;
        AvailabilityChangeEvent.publish(applicationContext, newState);
        log.info("changed readiness state to {} msg: {}", newState, msg);
    }

    @Scheduled(cron = "${state.liveness.cron:-}")
    public void liveness() {
        LivenessState newState = availability.getLivenessState() == LivenessState.CORRECT ? LivenessState.BROKEN : LivenessState.CORRECT;
        AvailabilityChangeEvent.publish(applicationContext, newState);
        log.info("changed liveness state to {} msg: {}", newState, msg);
    }

}
