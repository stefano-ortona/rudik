package asu.edu.rule_miner.rudik.resources;

import asu.edu.rule_miner.rudik.views.IndexView;
import asu.edu.rule_miner.rudik.api.RudikApi;
import com.codahale.metrics.annotation.Timed;
import asu.edu.rule_miner.rudik.api.model.RudikResult;
import asu.edu.rule_miner.rudik.api.model.HornRuleResult;
import asu.edu.rule_miner.rudik.model.horn_rule.HornRule;
import asu.edu.rule_miner.rudik.model.horn_rule.RuleAtom;
import javax.ws.rs.GET;
import java.util.*;
import java.io.*;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

@Path("/rudik")
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
/*@Produces(MediaType.APPLICATION_JSON)*/

public class IndexResource {

    public IndexResource() {
                    
             }

    @GET
    @Timed
public IndexView IndexPage() {

        return new IndexView();
 }

}
