<!doctype html>
<html lang="en">
<head>
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>

    <!-- Bootstrap CSS -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/css/bootstrap.min.css" integrity="sha384-Smlep5jCw/wG7hdkwQ/Z5nLIefveQRIY9nfy6xoR1uRYBtpZgI6339F5dgvm/e9B" crossorigin="anonymous">
    <link href='https://fonts.googleapis.com/css?family=Sofia' rel='stylesheet'>
    <!-- Custom styles for this template -->
   <!-- <link href="rudik.css" rel="stylesheet"> -->
    <link rel=" icon" type="image/png" href="assets/images/RuDiK_icon32.png">
  <title>RuDiK - Rules discovery in Knowledge bases</title>

    <!-- loader when submitting the forms rudik -->
    <script>
        $(document).ready(function(){
            $("#discoverRulesForm").on('submit', function(e){
                e.preventDefault();
                $("#overlay").toggle();
                this.submit();
            });
            $("#instantiateRuleForm").on('submit', function(e) {
                e.preventDefault();
                $("#overlay").toggle();
                this.submit();
            });
        });

    </script>
<!-- Hide the loader page when clicking browser back button -->
    <script type="text/javascript">
    function reloadOnPreviousPageButton() {
        if (performance.navigation.type == 2) {
            //location.reload(true);
            document.getElementById("overlay").style.display = "none";
        }
    }
    </script>

</head>

<style>
.container {padding-bottom: 13.75%}
.loader {
    border: 16px solid #f3f3f3;
    border-radius: 50%;
    border-top: 16px solid #00aeff; /* Blue */
    border-right: 16px solid #90fc3b;
    border-bottom: 16px solid #ff0b0b;
    width: 120px;
    height: 120px;
    -webkit-animation: spin 2s linear infinite;
    animation: spin 2s linear infinite;


    position: absolute;
    top:0;
    bottom: 0px;
    z-index: 1001;
    left: 0;
    right: 0;

    margin: auto;
   }
.overlay {
    background-image: url('assets/images/Background_overlay.png');
    display: none;
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 1001;
}


@-webkit-keyframes spin {
    0% { -webkit-transform: rotate(0deg); }
    100% { -webkit-transform: rotate(360deg); }
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>

<body onload="reloadOnPreviousPageButton()">
<!--loader -->
<div class="overlay" id="overlay">
<div class="loader" id="loader"></div>
</div>

<!--nav bar logo + text -->
<nav class="navbar navbar-dark bg-dark">
    <img src="assets/images/Logo_RuDiK.png" width="12%" height="12%">
     <a class="rudik_header" href="/rudik" style="font-family: 'Segoe UI'; font-size: 40px;text-align: center; padding-right: 22%;"><span style="color: #00aeff;">RuDiK: Rule</span> <span style="color: #90fc3b;"> Discovery</span><span style="color: #ff0b0b;"> in Knowledge Bases</span> </a>
</nav>
<!--end of nav bar logo + text -->

<br>
<!--Configuration parameters form-->
<div class="container">
    <div class="row">
        <!-- main parameters -->
    <div class="col-sm-6">
<!--beginning if the form -->
	<form name="discoverRulesForm" id ="discoverRulesForm" action="/discover-rules" >

        <div class="card text-white " style="width: auto; " >

            <div class="card-header"><h2 style="color: black;">Discover new rules</h2></div>
            <div class="card-body text-dark">
                <!-- define the two cards: main parameters + more options -->
                <ul class="nav nav-tabs" id="myTab" role="tablist">
                    <li class="nav-item">
                        <a class="nav-link active" id="home-tab" data-toggle="tab" href="#home" role="tab" aria-controls="home" aria-selected="true">Main parameters</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" id="options-tab" data-toggle="tab" href="#options" role="tab" aria-controls="options" aria-selected="false">More options</a>
                    </li>

                </ul>
                <!-- end of define the two cards: main parameters + more options -->

                <div class="tab-content" id="myTabContent">
                    <!-- first card for main parameters -->

                    <div class="tab-pane fade show active" id="home" role="tabpanel" aria-labelledby="home-tab">
        
            <!--parameters: type of rules -->

            <fieldset class="form-group">
                <div class="row">
                    <label class="col-form-label col-sm-4 pt-0" data-toggle="tooltip" data-placement="top" title=" A positive rule enriches the KB with new facts and thus increases its coverage.  A negative rule spots logical inconsistencies and identifies erroneous triples">Type of rules</label>
                    <div class="col-sm-8">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="rulesType" id="test_pos" value="pos" checked>
                           <label class="form-check-label" for="test_pos">
                                Positive
                            </label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="rulesType" id="test_neg" value="neg">
                            <label class="form-check-label" for="test_neg">
                                Negative
                            </label>
                        </div>
                    </div>
                </div>
            </fieldset>

	    <!--parameter: knowledge graph -->

            <div class="form-group row">
                <label class="control-label col-sm-4" for="KG" data-toggle="tooltip
" data-placement="top" title="The Knowledge Graph where to look for rules">Knowledge graph </label>
                <div class="col-sm-5">
                    <select class="form-control" id="KG" name="kg" required>
                        <option  value="">Choose...</option>
                        <option value="dbpedia" selected>dbpedia</option>
                        <option value="yago">yago</option>

                    </select>
                </div>
            </div>

            <!--parameter: target predicate -->
                        <div class="form-group row">
                            <label for="predicate" class="col-sm-4 col-form-label" data-toggle="tooltip" data-placement="top" title="The predicate for which we want to find some rules!">Target predicate</label>
                            <div class="col-sm-5">
                                <input type="text" class="form-control" placeholder="Select or enter a predicate" list="predicate" name="predicate" required />
                                <datalist id="predicate">

                                </datalist>

                            </div>
                        </div>

            <!--run button -->
            <div class="form-group row">
                <div class="col-sm-10">
                    <button id= "discoverRulesButton" type="submit" class="btn btn-primary">Run</button>
                </div>
            </div>
        
    </div>

                    <!-- second card for more options -->
                    <div class="tab-pane fade" id="options" role="tabpanel" aria-labelledby="options-tab">
                        <br>

                        <!--parameter: endpoint-->

                        <div class="col-sm-12">
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text"   data-toggle="tooltip" data-placement="top"  title="This parameter defines the endpoint to query the target Knowledge Graph.">Sparql endpoint</div>
                                    <select class="form-control" id="endpoint" name="endpoint" class="required" >
                                        <option value="http://dbpedia.org/sparql">dbpedia online endpoint</option>
                                        <option value="http://localhost:8890/sparql" selected>Virtuoso on localhost</option>
                                        <option value="https://linkeddata1.calcul.u-psud.fr/sparql">Yago online endpoint</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <!--parameter: alpha -->

                        <div class="col-sm-6">
                            <label class="sr-only" for="alpha" >Alpha</label>
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="A high α champions the recall by favoring rules covering more generation examples">&alpha;</div>
                                </div>
                                <input type="number" name="alpha" class="form-control" id="alpha" value="0.3" placeholder="0<=&alpha;<=1" step="0.1" min="0" max="1">
                            </div>
                        </div>

                        <!--parameter: gamma -->

                        <div class="col-sm-6">
                            <label class="sr-only" for="gamma" >Gamma</label>
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="">&gamma;</div>
                                </div>
                                <input type="number" name="gamma" class="form-control" id="gamma" value="0.1" step="0.1" min="0" max="1">
                            </div>
                        </div>


                        <!--parameter: validation threshold
                        <div class="col-sm-6">
                            <label class="sr-only" for="threshold"> Threshold</label>
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="Validation threshold"> Threshold</div>
                                </div>
                                <input type="number" class="form-control" id="threshold" value="0.2" name="threshold" step="0.1">
                            </div>
                        </div>-->

                        <!--parameter: Number of threads
                        <div class="col-sm-6">
                            <label class="sr-only" for="num_threads">Number of threads</label>
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text">Number of threads</div>
                                </div>
                                <input type="number" class="form-control" value="1" id="num_threads" placeholder="" name="num_threads">
                            </div>
                        </div>   -->

                        <!--parameter: Max rule length -->
                        <div class="col-sm-6">
                            <div class="input-group mb-2">
                                <div class="input-group-prepend">
                                    <div class="input-group-text"   data-toggle="tooltip" data-placement="top" title="It determines the maximum number of edges in the path, i.e., the maximum number of atoms allowed in the corresponding body of the rule. The higher is this parameter, the bigger will be the search space (hence the slower the computation)">Max rule length</div>
                                </div>
                                <input type="number" class="form-control" value="2" id="max_rule_length" placeholder="" name="max_rule_length">
                            </div>
                        </div>


                        <!--parameter: edges -->
                        <fieldset class="form-group">
                            <div class="row">
                                <div class="col-sm-7">
                                    <div class="col-auto">
                                        <label class="sr-only" for="sub_edges">Number of subject edges</label>
                                        <div class="input-group mb-2">
                                            <div class="input-group-prepend">
                                                <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="Limit incoming edges for an entity. -1 for unlimited">Nb of subject edges</div>
                                            </div>
                                            <input type="number" value="50" class="form-control" id="sub_edges" placeh
                                                   older="" name="sub_edges">
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <label class="sr-only" for="obj_edges">Number of object edges</label>
                                        <div class="input-group mb-2">
                                            <div class="input-group-prepend">
                                                <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="Limit outgoing edges for an entity. -1 for unlimited">Nb of object edges</div>
                                            </div>
                                            <input type="number" class="form-control" value="50" id="obj_edges" pl
                                                   aceholder="" name="obj_edges">

                                        </div>
                                    </div>
                                </div>
                            </div>
                        </fieldset>


                        <!--parameter: Number of examples -->
                        <fieldset class="form-group">
                            <div class="row">
                                <div class="col-sm-7">
                                    <div class="col-auto">
                                        <label class="sr-only" for="nb_pos" >Positive</label>
                                        <div class="input-group mb-2" >
                                            <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="This parameter define the number of input positive examples to be used for the mining (-1 for unlimited). The higher is this number, the bigger is the search space, but also the higher the chance to find valid rules. We found that an acceptable compromise between runtime and output quality can always be achieved with 500-1K examples.">
                                                <div class="input-group-prepend">Nb of pos examples</div>
                                            </div>
                                            <input type="number" value="10" class="form-control" id="nb_pos" placeholder="" name="nb_pos">
                                        </div>
                                    </div>
                                    <div class="col-auto">
                                        <label class="sr-only" for="nb_pos" >Negative</label>
                                        <div class="input-group mb-2">
                                            <div class="input-group-prepend">
                                                <div class="input-group-text" data-toggle="tooltip" data-placement="top" title="This parameter define the number of input negative examples to be used for the mining (-1 for unlimited). The higher is this number, the bigger is the search space, but also the higher the chance to find valid rules. We found that an acceptable compromise between runtime and output quality can always be achieved with 500-1K examples.">Nb of neg examples</div>
                                            </div>
                                            <input type="number" class="form-control" value="10" id="nb_neg" placeholder="" name="nb_neg">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </fieldset>

                        <!--parameter: include literals -->
                        <fieldset class="form-group">
                            <div class="row">
                                <label class="col-form-label col-sm-3 pt-0" data-toggle="tooltip" data-placement="top" title="decide whether to include literal values or not in the output rules.">Include literals</label>
                                <div class="col-sm-5">
                                    <div class="form-check">
                                        <input class="form-check-input" type="radio" name="includeLiterals" id="include_lit" value="yes" checked>
                                        <label class="form-check-label" for="include_lit">
                                            True
                                        </label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="radio" name="includeLiterals" id="no_lit" value="no">
                                        <label class="form-check-label" for="no_lit">
                                            False
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </fieldset>

                        <!--parameter: gamma -->

                        <div class="col-sm-6">
                            <div class="input-group mb-2">
                                <label for="sampling" >Sampling</label>
                                <input type="checkbox" name="sampling" class="form-control" id="sampling" checked>
                            </div>
                        </div>

    </form>
    </div>

    </div>
</div>
</div>
</form>
<!--end of the form -->
    
    </div>

<!-- test a rule card -->
    <div class="col-sm-6">

      <form action="/instantiate-rule" id="instantiateRuleForm" >

        <div class="card border-primary mb-3" style="width: auto;">

            <div class="card-header"><h2>Instantiate a rule</h2></div>
            <br>
            <!--parameter: knowledge graph -->

            <div class="form-group row" style="margin-left: 15px;">
                <label class="control-label col-sm-3" for="instKg" data-toggle="tooltip" data-placement="top" title="The Knowledge Graph where to look for rules">Knowledge graph </label>
                <div class="col-sm-7">
                    <select class="form-control" id="instKg" name="instKg" required>
                        <option selected value="">Choose...</option>
                        <option>dbpedia</option>
                        <option>yago</option>

                    </select>
                </div>
            </div>

            <!--parameter: sparql endpoint -->

            <div class="form-group row" style="margin-left: 15px;">
                <label class="control-label col-sm-3" for="instEndpoint" data-toggle="tooltip" data-placement="top" title="This parameter defines the endpoint to query the target Knowledge Graph.">Sparql endpoint</label>
                <div class="col-sm-7">
                    <select class="form-control" id="instEndpoint" name="instEndpoint" required>
                        <option selected value="">Choose...</option>
                    </select>
                </div>
            </div>

            <!--parameter: select a rule -->
            <div class="form-group row" style="margin-left: 15px;">
                <label for="rule_test" class="col-sm-3 col-form-label" data-toggle="tooltip" data-placement="left" title="Select a rule to test over the knowledge base">Select a rule</label>
                <div class="col-sm-7">
                    <input class="form-control" placeholder="Select or enter a rule" list="rule_test"  name="rule" required />
                    <datalist id="rule_test">

                    </datalist>

                </div>
            </div>
        

            <!--run button -->
            <div class="form-group row" style="margin-left: 15px;">
                <div class="col-sm-10">
                    <button id="instantiateRuleButton" type="submit" class="btn btn-primary">Run</button>
                </div>
            </div>

        </div>
      </form>

    </div>
    </div>

</div>

<!--Js to display rules based on the selected knowledge base -->
<script>
//function to change the list of rules to instantiate according to the kg selected
$(document).ready(function() {

    $("#instKg").change(function() {
        var val = $(this).val();
        if (val == "dbpedia") {

            $("#rule_test").html(" <option value='' selected>Select a rule for dbpedia...</option><option>parent(v0,subject) & parent(v0,object) => spouse(subject,object)</option><option>birthDate(subject,v0) & deathDate(object,v1) & >(v0,v1) => not spouse(subject,object)</option><option>foundingYear(subject,v0) & birthDate(object,v1) & >(v0,v1) => not foundedBy(subject,object)</option><option>spouse(subject,http://dbpedia.org/resource/Barbara_Henrickson) & child(object,http://dbpedia.org/resource/Barbara_Henrickson) => spouse(subject,object)</option>");
        } else if (val == "yago") {
            $("#rule_test").html(" <option value='' selected>Select a rule for yago...</option><option>isLeaderOf(v0,object) & isLeaderOf(v0,v1) & isLeaderOf(subject,v1) => isLeaderOf(subject,object)</option><option>hasChild(subject,v0) & hasChild(object,v0) => isMarriedTo(subject,object)</option>");

        }
    });

});
//function to change the endpoints according to the selected kg
$(document).ready(function() {
    $("#instKg").change(function () {
        var value =$(this).val();
        if (value == "dbpedia") {
            $("#instEndpoint").html("<option value=\"http://dbpedia.org/sparql\">dbpedia online endpoint</option><option value=\"http://localhost:8890/sparql\" selected>Virtuoso on localhost</option>");
        } else if (value == "yago") {
            $("#instEndpoint").html("<option value=\"http://localhost:8890/sparql\" selected>Virtuoso on localhost</option><option value=\"https://linkeddata1.calcul.u-psud.fr/sparql\">Yago online endpoint</option>");
        }
    });
});
//function to change the predicates available according to the selected kg
$(document).ready(function() {

    $("#KG").change(function() {
        var val = $(this).val();
        if (val == "dbpedia") {
            var options='<option selected>Select a predicate for dbpedia...</option><option>academicAdvisor</option><option>child</option><option>founder</option><option>spouse</option><option>successor</option>';
            document.getElementById("predicate").innerHTML=options;
            $("#predicate").html("<option value='' selected>Select a predicate for dbpedia...</option><option>academicAdvisor</option><option>child</option><option>founder</option><option>spouse</option><option>successor</option>");
        } else if (val == "yago") {
            $("#predicate").html("<option value='' selected>Select a predicate for yago...</option><option>exports</option><option>hasChild</option><option>influences</option><option>isLeaderOf</option><option>isMarriedto<option>");

        }
    });

});
</script>
<!-- Optional JavaScript -->
<!-- jQuery first, then Popper.js, then Bootstrap JS -->
<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js" integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49" crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/js/bootstrap.min.js" integrity="sha384-o+RDsa0aLu++PJvFqy8fFScvbHFLtbvScb8AjopnFD+iEQ7wo/CG0xlczd+2O/em" crossorigin="anonymous"></script>
<script>
    $(document).ready(function(){
        $('[data-toggle="tooltip"]').tooltip();
    });
</script>
<br>
<!-- Footer -->
<footer  style="background-color: #1c2331; " >

    <!-- Footer Links -->
    <div class="container-fluid text-center text-md-left">

        <!-- Grid row -->
        <div class="row">

            <!-- Grid column -->
            <div class="col-md-6 mt-md-0 mt-3" style="color: white;">

                <!-- Content -->
                <h5 class="text-uppercase">What is RuDiK?</h5>
                <p>RuDiK is a system for discovering positive and negative logical rules over RDF knowledge graphs. The prototype is available for download on  <a href="https://github.com/stefano-ortona/rudik/">github</a> and the ICDE technical paper is available <a href=" https://www.dropbox.com/s/4hgcli75ccqe20t/Rudik_CR_ICDE.pdf?dl=0">here</a>.</p>

            </div>
            <!-- Grid column -->

            <hr class="clearfix w-100 d-md-none pb-3">

            <!-- Grid column -->
            <div class="col-md-3 mb-md-0 mb-3">

                <!-- Links -->
                <h5 class="text-uppercase">Links</h5>

                <ul class="list-unstyled">
                    <li>
                        <a href="mailto:Paolo.Papotti@eurecom.fr">Contact</a>
                    </li>

                </ul>

            </div>
            <!-- Grid column -->


        </div>
        <!-- Grid row -->

    </div>
    <!-- Footer Links -->

</footer>
<!-- Footer -->


</body>

</html>


