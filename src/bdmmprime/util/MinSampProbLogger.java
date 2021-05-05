package bdmmprime.util;

import bdmmprime.distribution.BirthDeathMigrationDistribution;
import beast.core.BEASTObject;
import beast.core.Input;
import beast.core.Loggable;

import java.io.PrintStream;

public class MinSampProbLogger extends BEASTObject implements Loggable {

    public Input<BirthDeathMigrationDistribution> distribInput = new Input<>(
            "distrib",
            "Birth-death migration distribution object",
            Input.Validate.REQUIRED);

    @Override
    public void initAndValidate() {

    }

    @Override
    public void init(PrintStream out) {
        if (getID() != null)
            out.print(getID() + "\t");
        else
            out.println("minSampProb\t");
    }

    @Override
    public void log(long sample, PrintStream out) {
        out.print(distribInput.get().calculateLogMinSampleProb(
                distribInput.get().conditionOnMinPsiSamplesInput.get(),
                distribInput.get().parameterizationInput.get().originInput.get().getArrayValue())
                + "\t");
    }

    @Override
    public void close(PrintStream out) {

    }
}
