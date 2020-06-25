package bdmmprime.trajectories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Trajectory {
    public List<TrajectoryEvent> events = new ArrayList<>();
    public double[] currentState;

    public Trajectory(double[] currentState) {
        this.currentState = currentState.clone();
    }

    public Trajectory(double[] currentState, List<TrajectoryEvent> events) {
        this.currentState = currentState.clone();
        this.events = new ArrayList<>(events);
    }

    public Trajectory copy() {
        return new Trajectory(currentState, events);
    }

    public void addEvent(TrajectoryEvent event) {
        events.add(event);
        event.updateState(currentState);
    }

    public List<double[]> getStateList() {
        List<double[]> states = new ArrayList<>();

        double[] state = currentState.clone();
        states.add(state.clone());
        for (int i=events.size()-1; i>=0; i--) {
            events.get(i).reverseUpdateState(state);
            states.add(state.clone());
        }

        Collections.reverse(states);

        return states;
    }

    public List<Double> getEventTimes() {
        return this.events.stream().map(e -> e.time).collect(Collectors.toList());
    }
}
