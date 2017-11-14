package TestTopology.testforls;

import TestTopology.TestPassiveShedding.Output2;
import TestTopology.simulated.TASentenceSpout;
import TestTopology.simulated.TASplitSentence;
import TestTopology.simulated.TAWordCounter;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import resa.util.ConfigUtil;

import java.io.File;

/**
 * Created by kailin on 2017/7/22.
 */
public class SheddingSimExpServWC {
    public static void main(String[] args) throws Exception {

        Config conf = ConfigUtil.readConfig(new File(args[1]));

        if (conf == null) {
            throw new RuntimeException("cannot find conf file " + args[0]);
        }

        TopologyBuilder builder = new TopologyBuilder();
        //TopologyBuilder builder = new ResaTopologyBuilder();
        int defaultTaskNum = ConfigUtil.getInt(conf, "defaultTaskNum", 10);

        String host = (String)conf.get("redis.host");
        int port = ConfigUtil.getInt(conf, "redis.port", 6379);
        String queueName = (String)conf.get("redis.sourceQueueName");

        builder.setSpout("chain-Spout", new TASentenceSpout(host, port, queueName),
                ConfigUtil.getInt(conf, "chain-spout.parallelism", 1));

        double chainBoltA_mu = ConfigUtil.getDouble(conf, "chain-BoltA.mu", 1.0);
        builder.setBolt("chain-BoltA", new TASplitSentence(() -> (long) (-Math.log(Math.random()) * 1000.0 / chainBoltA_mu)),
                ConfigUtil.getInt(conf, "chain-BoltA.parallelism", 1))
                .setNumTasks(defaultTaskNum)
                .shuffleGrouping("chain-Spout");

        double chainBoltB_mu = ConfigUtil.getDouble(conf, "chain-BoltB.mu", 1.0);
        builder.setBolt("chain-BoltB", new TAWordCounter(() -> (long) (-Math.log(Math.random()) * 1000.0 / chainBoltB_mu)),
                ConfigUtil.getInt(conf, "chain-BoltB.parallelism", 1))
                .setNumTasks(defaultTaskNum)
                .shuffleGrouping("chain-BoltA");

        builder.setBolt("out",new Output2(() ->2L),1).shuffleGrouping("chain-BoltB");

        conf.setNumWorkers(ConfigUtil.getInt(conf, "chain-NumOfWorkers", 1));
        //conf.setMaxSpoutPending(ConfigUtil.getInt(conf, "chain-MaxSpoutPending", 0));
        conf.setDebug(ConfigUtil.getBoolean(conf, "DebugTopology", false));
        conf.setStatsSampleRate(ConfigUtil.getDouble(conf, "StatsSampleRate", 1.0));

        //ResaConfig resaConfig = ResaConfig.create();
        //resaConfig.putAll(conf);
        //resaConfig.addDrsSupport();
        //resaConfig.put(ResaConfig.REBALANCE_WAITING_SECS, 0);
        //System.out.println("ResaMetricsCollector is registered");
        //conf.registerMetricsConsumer(LoggingMetricsConsumer.class);
        StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
    }
}
