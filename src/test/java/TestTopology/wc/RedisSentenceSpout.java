package TestTopology.wc;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import resa.shedding.tools.FrequencyRestrictor;
import resa.topology.RedisQueueSpout;

import java.util.Map;

public class RedisSentenceSpout extends RedisQueueSpout {

    private transient long count = 0;
    private String spoutIdPrefix = "s-";
    private FrequencyRestrictor frequencyRestrictor;

    public RedisSentenceSpout(String host, int port, String queue) {
        super(host, port, queue);
    }

    @Override
    public void open(Map map, TopologyContext context, SpoutOutputCollector collector) {
        super.open(map, context, collector);
        spoutIdPrefix = spoutIdPrefix + context.getThisTaskId() + '-';
        frequencyRestrictor = new FrequencyRestrictor(500, 500);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sid", "sentence"));
    }

    @Override
    protected void emitData(Object data) {
        String id = spoutIdPrefix + count;
        count++;
        collector.emit(new Values(id, data), id);
    }

    @Override
    public void nextTuple() {
        if(frequencyRestrictor.tryPermission()) {
            super.nextTuple();
        }
    }

    @Override
    public void ack(Object msgId) {
    }

    @Override
    public void fail(Object msgId) {

    }
}