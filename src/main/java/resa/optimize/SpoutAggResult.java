package resa.optimize;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ding on 14-5-6.
 *
 * Modified by Tom Fu on 21-Dec-2015, for new DisruptQueue Implementation for Version after storm-core-0.10.0
 * Functions and Classes involving queue-related metrics in the current class will be affected:
 *
 * Here we need to distinguish aggregate result at different levels, e.g., tasks to executors, executors to components, components in history window
 */
public class SpoutAggResult extends AggResult {

    private Map<String, CntMeanVar> completedLatency = new HashMap<>();



    private Map<String, Long> shedRelateCount = new HashMap<>();

    public Map<String, CntMeanVar> getCompletedLatency() {
        return completedLatency;
    }

    public CntMeanVar getCombinedCompletedLatency() {
        CntMeanVar retVal = new CntMeanVar();
        completedLatency.values().stream().forEach(retVal::addCMV);
        return retVal;
    }

    @Override
    public void add(AggResult r) {
        super.add(r);
        ((SpoutAggResult) r).completedLatency.forEach((s, cntMeanVar) ->
                this.completedLatency.computeIfAbsent(s, (k) -> new CntMeanVar()).addCMV(cntMeanVar));
        ((SpoutAggResult) r).getShedRelateCount().forEach((stream, number) -> {
            if(this.getShedRelateCount().containsKey(stream)) {
                long temp = this.getShedRelateCount().get(stream);
                temp +=  ((SpoutAggResult) r).getShedRelateCount().get(stream);
                this.getShedRelateCount().put(stream, temp);
            } else {
                this.getShedRelateCount().put(stream, ((SpoutAggResult) r).getShedRelateCount().get(stream));
            }
        });
    }

    public double getAvgTupleCompleteLatency(){
        return this.getCombinedCompletedLatency().getAvg();
    }

    public double getScvTupleCompleteLatency(){
        return this.getCombinedCompletedLatency().getScv();
    }

    public long getNumOfCompletedTuples(){
        return this.getCombinedCompletedLatency().getCount();
    }

    public Map<String, Long> getShedRelateCount() {
        return shedRelateCount;
    }

//    public int getFailureCount() {
//        return failureCount;
//    }
//
//    public int getSpoutDropCount() {
//        return spoutDropCount;
//    }
//
//    public int getFailLatencyMs() {
//        return failLatencyMs;
//    }
//
//    public int getActiveSpoutDropCount() {
//        return activeSpoutDropCount;
//    }
}
