package asu.edu.rule_miner.rudik;

import asu.edu.rule_miner.rudik.resources.DiscoverRulesResource;
import asu.edu.rule_miner.rudik.resources.IndexResource;
import asu.edu.rule_miner.rudik.resources.InstantiateRuleResource;
//import asu.edu.rule_miner.rudik.resources.GraphResource;

import java.util.Map;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.assets.AssetsBundle;

public class RudikApplication extends Application<RudikConfiguration> {
    public static void main(String[] args) throws Exception {
        new RudikApplication().run(args);
    }


    @Override
    public void initialize(Bootstrap<RudikConfiguration> bootstrap) {
         bootstrap.addBundle(new ViewBundle<RudikConfiguration>() {
        @Override
        public Map<String, Map<String, String>> getViewConfiguration(RudikConfiguration config) {
            return config.getViewRendererConfiguration();
        }
    });
	 //bootstrap.addBundle(new AssetsBundle());
	 bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(RudikConfiguration configuration,
                    Environment environment) {
        final DiscoverRulesResource resource = new DiscoverRulesResource(
    );
   	final IndexResource indexResource = new IndexResource();
        final InstantiateRuleResource instantiateRuleResource = new InstantiateRuleResource();
       //final GraphResource graphResource = new GraphResource();
    environment.jersey().register(resource);
    environment.jersey().register(instantiateRuleResource);
    environment.jersey().register(indexResource);
    //environment.jersey().register(graphResource);
    }

}
