package resa.optimize;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ding on 14-5-6.
 */
public class BoltAggResult extends AggResult {

    private Map<String, CntMeanVar> tupleProcess = new HashMap<>();

    public Map<String, CntMeanVar> getTupleProcess() {
        return tupleProcess;
    }

    protected Map<String, Long> passiveSheddingCountMap = new HashMap<>();//tkl

    public CntMeanVar getCombinedProcessedResult() {
        CntMeanVar retVal = new CntMeanVar();
        tupleProcess.values().stream().forEach(retVal::addCMV);
        return retVal;
    }

    @Override
    public void add(AggResult r) {
        super.add(r);
        ((BoltAggResult) r).tupleProcess.forEach((s, cntMeanVar) ->
                this.tupleProcess.computeIfAbsent(s, (k) -> new CntMeanVar()).addCMV(cntMeanVar));
        ((BoltAggResult) r).getPassiveSheddingCountMap().forEach((stream,count)->{
            if(this.passiveSheddingCountMap.containsKey(stream)){
                passiveSheddingCountMap.put(stream,passiveSheddingCountMap.get(stream)+count);
            }else{
                passiveSheddingCountMap.put(stream,count);
            }
        });
        //System.out.println("heihei"+passiveSheddingCountMap);
    }

    public double getAvgServTimeHis(){
        return this.getCombinedProcessedResult().getAvg();
    }

    public double getScvServTimeHis(){
        return this.getCombinedProcessedResult().getScv();
    }

    public long getNumCompleteTuples(){
        return this.getCombinedProcessedResult().getCount();
    }

    public Map<String, Long> getPassiveSheddingCountMap() {
        return passiveSheddingCountMap;
    }

}
