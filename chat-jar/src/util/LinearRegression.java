package util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinearRegression {

	public static Double predictForValue(List<Integer> x, List<Integer> y, int predictForDependentVariable) {
        if (x.size() != y.size())
            throw new IllegalStateException("Must have equal X and Y data points");

        Integer numberOfDataValues = x.size();

        List<Double> xSquared = x
                .stream()
                .map(position -> Math.pow(position, 2))
                .collect(Collectors.toList());

        List<Integer> xMultipliedByY = IntStream.range(0, numberOfDataValues)
                .map(i -> x.get(i) * y.get(i))
                .boxed()
                .collect(Collectors.toList());

        Integer xSummed = x
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        Integer ySummed = y
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        Double sumOfXSquared = xSquared
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        Integer sumOfXMultipliedByY = xMultipliedByY
                .stream()
                .reduce((prev, next) -> prev + next)
                .get();

        int slopeNominator = numberOfDataValues * sumOfXMultipliedByY - ySummed * xSummed;
        Double slopeDenominator = numberOfDataValues * sumOfXSquared - Math.pow(xSummed, 2);
        Double slope = slopeNominator / slopeDenominator;

        double interceptNominator = ySummed - slope * xSummed;
        double interceptDenominator = numberOfDataValues;
        Double intercept = interceptNominator / interceptDenominator;

        return (slope * predictForDependentVariable) + intercept;
    }
}
