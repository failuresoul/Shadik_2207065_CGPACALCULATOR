package org.example.demo;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;

/**
 * Performs GPA calculations using parallel processing
 * Demonstrates Java Concurrency with Fork/Join framework
 */
public class ConcurrentGPACalculator {

    private final ForkJoinPool forkJoinPool;

    public ConcurrentGPACalculator() {
        // Use common pool for parallel operations
        this.forkJoinPool = ForkJoinPool.commonPool();
    }

    /**
     * Calculate GPA using parallel streams for better performance
     * @param courses List of courses
     * @return Calculated GPA
     */
    public double calculateGPAParallel(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }

        try {
            return forkJoinPool.submit(() -> {
                // Calculate total weighted points in parallel
                double totalWeightedPoints = courses.parallelStream()
                        .mapToDouble(Course::getWeightedGradePoint)
                        .sum();

                // Calculate total credits in parallel
                double totalCredits = courses.parallelStream()
                        .mapToDouble(Course::getCourseCredit)
                        .sum();

                return totalWeightedPoints / totalCredits;
            }).get();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Error calculating GPA: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Get total credits using parallel processing
     */
    public double getTotalCreditsParallel(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }

        return courses.parallelStream()
                .mapToDouble(Course::getCourseCredit)
                .sum();
    }

    /**
     * Get courses grouped by grade using concurrent operations
     */
    public ConcurrentHashMap<String, List<Course>> groupCoursesByGrade(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return new ConcurrentHashMap<>();
        }

        return courses.parallelStream()
                .collect(Collectors.groupingByConcurrent(
                        Course::getGrade,
                        ConcurrentHashMap::new,
                        Collectors.toList()
                ));
    }

    /**
     * Calculate average grade point using parallel processing
     */
    public double getAverageGradePoint(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0.0;
        }

        return courses.parallelStream()
                .mapToDouble(Course::getGradePoint)
                .average()
                .orElse(0.0);
    }

    /**
     * Count courses with specific criteria using parallel stream
     */
    public long countCoursesAboveGrade(List<Course> courses, double minGradePoint) {
        if (courses == null || courses.isEmpty()) {
            return 0;
        }

        return courses.parallelStream()
                .filter(course -> course.getGradePoint() >= minGradePoint)
                .count();
    }

    /**
     * Get statistical summary using parallel operations
     */
    public GPAStatistics getStatistics(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return new GPAStatistics(0, 0, 0, 0, 0);
        }

        DoubleSummaryStatistics stats = courses.parallelStream()
                .mapToDouble(Course::getGradePoint)
                .summaryStatistics();

        return new GPAStatistics(
                stats.getCount(),
                stats.getSum(),
                stats.getAverage(),
                stats.getMin(),
                stats.getMax()
        );
    }

    /**
     * Inner class to hold GPA statistics
     */
    public static class GPAStatistics {
        public final long count;
        public final double sum;
        public final double average;
        public final double min;
        public final double max;

        public GPAStatistics(long count, double sum, double average, double min, double max) {
            this.count = count;
            this.sum = sum;
            this.average = average;
            this.min = min;
            this.max = max;
        }

        @Override
        public String toString() {
            return String.format(
                    "Statistics: Count=%d, Average=%.2f, Min=%.2f, Max=%.2f",
                    count, average, min, max
            );
        }
    }
}