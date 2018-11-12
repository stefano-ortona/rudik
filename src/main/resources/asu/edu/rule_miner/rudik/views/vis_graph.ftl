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
    <link rel=" icon" type="image/png" href="/assets/images/RuDiK_icon32.png">

<!-- style for vis.js -->

  <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.js"></script>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.css" />

<title>RuDiK - Rules discovery in Knowledge bases</title>
 </head>
<style>

 body {
        position: relative;
        margin: 0;
        min-height: 100%;
       }
    footer {
        right: 0;
        left: 0;
                }
    .mynetwork{
	height: 800px; 
	width: auto; 
	position: relative; 
	background:#747574 ;
	}

</style>
</html>

<body>
  <!--nav bar logo + text -->
<nav class="navbar navbar-dark bg-dark">
    <img src="/assets/images/Logo_RuDiK.png" width="12%" height="12%">
    <a class="rudik_header" href="/rudik" style="font-family: 'Segoe UI'; font-size:
 40px;text-align: center; padding-right: 20%;"><span style="color: #00aeff;">RuDiK: Rule</span> <span style="color: #90fc3b;"> Discovery</span><span style="color: #ff0b0b;"> in Knowledge Bases</span></a>
</nav>
<!--end of nav bar logo + text -->


   <p style="text-align: center; font-size: 30px;">Surrounding graph: <br> <span style="font-size: 20px";>Rule: ${instantiatedRule}</span> <br> <span style="font-size: 20px";>${example}</span> </p>

  <div class="mynetwork" id="mynetwork"></div>
   <script src="https://d3js.org/d3.v3.min.js" charset="utf-8"></script>

  <script type="text/javascript">

var json='${graph}';
var myGraph= json.replace(/&quot;/g, '\"');
var data =JSON.parse(myGraph);
   
var options = {
  autoResize: true,
  height: '100%',
  width: '100%',
    edges:{
        arrows: {
            to:     {enabled: false, scaleFactor:1, type:'arrow'},
            middle: {enabled: false, scaleFactor:1, type:'arrow'},
            from:   {enabled: true, scaleFactor:1, type:'arrow'}
        },
    },
  nodes : {
        //shape : 'dot',
        size : 100,
        color : '#00aeff', // select color

        font : {
            size : 16,
            //color : '#ffffff',
        },
        borderWidth : 2
    }

};

// create a network
    var container = document.getElementById('mynetwork');
    // initialize your network!
    var network = new vis.Network(container, data, options);

function clusterByCid() {
    network.setData(data);
var clusterOptionsByData = {
joinCondition:function(childOptions) {
    return childOptions.cid == 1;
},
clusterNodeProperties: {id:'cidCluster', borderWidth:3, shape:'database'}
};
network.cluster(clusterOptionsByData);
}

  </script>

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
                                                           <a href="mailto:Paolo.Papotti@eurecom.fr">Contact</a> </li> </ul>

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
