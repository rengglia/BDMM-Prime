package bdmmprime.trajectories.obsevents;

import bdmmprime.parameterization.Parameterization;
import bdmmprime.trajectories.Trajectory;
import bdmmprime.trajectories.trajevents.SamplingEvent;
import bdmmprime.util.Utils;
import beast.math.Binomial;
import beast.util.Randomizer;
import org.apache.commons.math.special.Gamma;

import static bdmmprime.util.Utils.nextBinomial;

public class ObservedSamplingEvent extends ObservedEvent {
    public int nLeaves, nSampledAncestors;

    public ObservedSamplingEvent(double time, int type, int nLeaves, int nSampledAncestors) {
        this.time = time;
        this.type = type;
        this.nLeaves = nLeaves;
        this.nSampledAncestors = nSampledAncestors;
    }

    @Override
    public int[] getNextLineageCounts() {
        int[] nextLineageCounts = lineages.clone();
        nextLineageCounts[type] -= nLeaves;

        return nextLineageCounts;
    }

    @Override
    public double applyToTrajectory(Parameterization param, int interval, Trajectory trajectory) {

        double logWeightContrib = 0;

        int s = type;
        int totalSamples = nSampledAncestors + nLeaves;

        if (Utils.equalWithPrecision(time, param.getIntervalEndTimes()[interval]) && param.getRhoValues()[interval][s]>0) {
            // Rho sampling

            // TODO Test this!

            // Probability of sample count
            logWeightContrib +=
                    Binomial.logChoose((int)Math.round(trajectory.currentState[s]), totalSamples) +
                    totalSamples*Math.log(param.getRhoValues()[interval][s])
                    + (trajectory.currentState[s]-totalSamples)*Math.log(1.0-param.getRhoValues()[interval][s]);

            logWeightContrib += Gamma.logGamma(totalSamples + 1);

            // Probability of known non-removal count:
            if (nSampledAncestors > 0)
                logWeightContrib += nSampledAncestors*Math.log(1.0 - param.getRemovalProbs()[interval][s]);

            if (logWeightContrib == Double.NEGATIVE_INFINITY)
                return logWeightContrib; // May happen if we saw sampled ancestors that we weren't meant to.

            if (param.getRemovalProbs()[interval][s] == 1.0) {
                trajectory.addEvent(new SamplingEvent(time, s, nLeaves, 0));

            } else {
                int nUnremovedLeaves = nextBinomial(nLeaves, 1.0-param.getRemovalProbs()[interval][s]);

                trajectory.addEvent(new SamplingEvent(time, s,
                        nLeaves - nUnremovedLeaves,
                        nUnremovedLeaves + nSampledAncestors));

                // The following computes the probability of the _particular_ configuration of sampled ancestors
                // and non-sampled ancestors seen at this point on the tree, for the chosen number of non-removed samples
                // generated by the rho event.

                int k = lineages[s] - nLeaves;
                int S = nUnremovedLeaves + nSampledAncestors;
                int N = (int)Math.round(trajectory.currentState[s]);

                logWeightContrib += Binomial.logChoose(S, nSampledAncestors)
                        + Binomial.logChoose(N - S, k-nSampledAncestors)
                        - Binomial.logChoose(N, k);
            }

        } else {
            // Psi sampling

            for (int i = 0; i < nLeaves; i++) {
                double sampling_prop = trajectory.currentState[s] * param.getSamplingRates()[interval][s];
                logWeightContrib += Math.log(sampling_prop);

                boolean isRemoval = (param.getRemovalProbs()[interval][s] == 1.0) ||
                        (Randomizer.nextDouble() < param.getRemovalProbs()[interval][s]);
                if (isRemoval) {
                    trajectory.addEvent(new SamplingEvent(time, s, 1, 0));
                } else {
                    logWeightContrib += Math.log(1.0 - (lineages[s] - 1.0) / trajectory.currentState[s]);
                    trajectory.addEvent(new SamplingEvent(time, s, 0, 1));
                }
            }

            for (int i = 0; i < nSampledAncestors; i++) {
                // The absence of N[s] in the following sampling propensity
                // is due to it being cancelled out by a 1/N[s] factor in the
                // tree event probability.
                double sampling_prop = param.getSamplingRates()[interval][s];
                logWeightContrib += Math.log((1.0 - param.getRemovalProbs()[interval][s]) * sampling_prop);
                trajectory.addEvent(new SamplingEvent(time, s, 0, 1));
            }

        }

        return logWeightContrib;
    }
}
