package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 6/5/17.
 */
@BlockerType(name = "timebased")
public class TimeBasedBlocker implements DeploymentBlockerFunction {

    private static final Logger logger = LoggerFactory.getLogger(TimeBasedBlocker.class);

    private TimeBasedBlockerConfiguration timeBasedBlockerConfiguration;

    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        timeBasedBlockerConfiguration = mapper.readValue(jsonConfiguration, TimeBasedBlockerConfiguration.class);
    }

    @Override
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment) {

        requireNonNull(timeBasedBlockerConfiguration);

        // Remember, 1 - Monday, 7 - Sunday
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        if (timeBasedBlockerConfiguration.getDaysOfTheWeek().stream().anyMatch(day -> day == dayOfWeek)) {
            try {
                int startHour = Integer.parseInt(timeBasedBlockerConfiguration.getStartTimeUtc().split(":")[0]);
                int startMinute = Integer.parseInt(timeBasedBlockerConfiguration.getStartTimeUtc().split(":")[1]);

                int endHour = Integer.parseInt(timeBasedBlockerConfiguration.getEndTimeUtc().split(":")[0]);
                int endMinute = Integer.parseInt(timeBasedBlockerConfiguration.getEndTimeUtc().split(":")[1]);

                LocalTime startTime = LocalTime.of(startHour, startMinute);
                LocalTime endTime = LocalTime.of(endHour, endMinute);

                LocalTime now = LocalTime.now();
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    logger.info("Now is {} which is after {} and before {}, blocking!", now.toString(), startTime.toString(), endTime.toString());
                    return true;
                }

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                logger.warn("Timebase blocker not configured correctly, could not parse the timeframe!", e);
            }
        }

        return false;
    }

    public static class TimeBasedBlockerConfiguration {
        private String startTimeUtc; // 15:32
        private String endTimeUtc;
        private List<Integer> daysOfTheWeek;

        public TimeBasedBlockerConfiguration() {
        }

        public String getStartTimeUtc() {
            return startTimeUtc;
        }

        public void setStartTimeUtc(String startTimeUtc) {
            this.startTimeUtc = startTimeUtc;
        }

        public String getEndTimeUtc() {
            return endTimeUtc;
        }

        public void setEndTimeUtc(String endTimeUtc) {
            this.endTimeUtc = endTimeUtc;
        }

        public List<Integer> getDaysOfTheWeek() {
            return daysOfTheWeek;
        }

        public void setDaysOfTheWeek(List<Integer> daysOfTheWeek) {
            this.daysOfTheWeek = daysOfTheWeek;
        }
    }
}
