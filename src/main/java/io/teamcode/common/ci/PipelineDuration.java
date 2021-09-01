package io.teamcode.common.ci;

import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.Pipeline;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.base.AbstractInterval;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gitlab Way:
 *
 * # # Introduction - total running time
 #
 # The problem this module is trying to solve is finding the total running
 # time amongst all the jobs, excluding retries and pending (queue) time.
 # We could reduce this problem down to finding the union of periods.
 #
 # So each job would be represented as a `Period`, which consists of
 # `Period#first` as when the job started and `Period#last` as when the
 # job was finished. A simple example here would be:
 #
 # * A (1, 3)
 # * B (2, 4)
 # * C (6, 7)
 #
 # Here A begins from 1, and ends to 3. B begins from 2, and ends to 4.
 # C begins from 6, and ends to 7. Visually it could be viewed as:
 #
 #     0  1  2  3  4  5  6  7
 #        AAAAAAA
 #           BBBBBBB
 #                       CCCC
 #
 # The union of A, B, and C would be (1, 4) and (6, 7), therefore the
 # total running time should be:
 #
 #     (4 - 1) + (7 - 6) => 4
 #
 # # The Algorithm
 #
 # The algorithm used here for union would be described as follow.
 # First we make sure that all periods are sorted by `Period#first`.
 # Then we try to merge periods by iterating through the first period
 # to the last period. The goal would be merging all overlapped periods
 # so that in the end all the periods are discrete. When all periods
 # are discrete, we're free to just sum all the periods to get real
 # running time.
 #
 # Here we begin from A, and compare it to B. We could find that
 # before A ends, B already started. That is `B.first <= A.last`
 # that is `2 <= 3` which means A and B are overlapping!
 #
 # When we found that two periods are overlapping, we would need to merge
 # them into a new period and disregard the old periods. To make a new
 # period, we take `A.first` as the new first because remember? we sorted
 # them, so `A.first` must be smaller or equal to `B.first`. And we take
 # `[A.last, B.last].max` as the new last because we want whoever ended
 # later. This could be broken into two cases:
 #
 #     0  1  2  3  4
 #        AAAAAAA
 #           BBBBBBB
 #
 # Or:
 #
 #     0  1  2  3  4
 #        AAAAAAAAAA
 #           BBBB
 #
 # So that we need to take whoever ends later. Back to our example,
 # after merging and discard A and B it could be visually viewed as:
 #
 #     0  1  2  3  4  5  6  7
 #        DDDDDDDDDD
 #                       CCCC
 #
 # Now we could go on and compare the newly created D and the old C.
 # We could figure out that D and C are not overlapping by checking
 # `C.first <= D.last` is `false`. Therefore we need to keep both C
 # and D. The example would end here because there are no more jobs.
 #
 # After having the union of all periods, we just need to sum the length
 # of all periods to get total time.
 #
 #     (4 - 1) + (7 - 6) => 4
 #
 # That is 4 is the answer in the example.

 *
 *
 */
public class PipelineDuration {

    /**
     * 파이프라인 전체 수행 시간을 계산합니다.
     *
     * @param pipeline
     * @return 밀리세컨드 단위입니다.
     */
    public static final long fromPipeline(Pipeline pipeline) {

        /*
        status = %w[success failed running canceled]
        builds = pipeline.builds.latest.
          where(status: status).where.not(started_at: nil).order(:started_at)

         */
        List<Job> filteredJobs = pipeline
                .getJobs()
                .stream().filter(
                    j -> j.getStartedAt() != null
                        && (j.getStatus() == PipelineStatus.SUCCESS || j.getStatus() == PipelineStatus.FAILED || j.getStatus() == PipelineStatus.RUNNING || j.getStatus() == PipelineStatus.CANCELED)).collect(Collectors.toList());

        return fromJobs(filteredJobs);
    }

    public static final long fromJobs(List<Job> jobs) {
        List<Interval> intervals = new LinkedList<>();
        for(Job job: jobs) {
            intervals.add(
                    new Interval(
                            new DateTime(job.getStartedAt()),
                            (job.getFinishedAt() == null ? DateTime.now() : new DateTime(job.getFinishedAt()))));
        }

        intervals.sort(Comparator.comparing(AbstractInterval::getStart));

        /*for (Interval interval: intervals) {
            System.out.println("--> sorted interval: " + interval);
        }*/

        long duration = processDuration(processIntervals(intervals));

        return duration;
    }

    private static final List<Interval> processIntervals(List<Interval> intervals) {
        if (intervals.isEmpty())
            return Collections.emptyList();

        List<Interval> mergedIntervals = new LinkedList<>();

        mergedIntervals.add(intervals.remove(0));

        for (Interval interval: intervals) {
            Interval previousInterval = mergedIntervals.get(mergedIntervals.size() - 1);
            if (isOverlap(previousInterval, interval)) {
                //System.out.println("Overlapped: " + previousInterval + ", --> " + interval);
                mergedIntervals.set(mergedIntervals.size() - 1, merge(previousInterval, interval));
            }
            else {
                mergedIntervals.add(interval);
            }
        }

        /*for (Interval interval: mergedIntervals) {
            System.out.println("merged interval: " + interval);
        }*/

        return mergedIntervals;
    }

    //current.first <= previous.last
    private static final boolean isOverlap(Interval previous, Interval current) {

        return previous.overlaps(current);
    }

    /**
     *
     def merge(previous, current)
     Period.new(previous.first, [previous.last, current.last].max)
     end
     *
     * @param previous
     * @param current
     * @return
     */
    public static final Interval merge(Interval previous, Interval current) {

        return new Interval(previous.getStart(), Collections.max(Arrays.asList(previous.getEnd(), current.getEnd())));
    }

    private static final long processDuration(List<Interval> intervals) {

        return intervals.stream().mapToLong(i -> i.toDurationMillis()).sum();
    }

}
