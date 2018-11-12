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

    <!-- script for google charts -->
    <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>


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

    </style>

<body>
<!--nav bar logo + text -->
<nav class="navbar navbar-dark bg-dark">
    <img src="/assets/images/Logo_RuDiK.png" width="12%" height="12%">
    <a class="rudik_header" href="/rudik" style="font-family: 'Segoe UI'; font-size:
 40px;text-align: center; padding-right: 20%;"><span style="color: #00aeff;">RuDiK: Rule</span> <span style="color: #90fc3b;"> Discovery</span><span style="color: #ff0b0b;"> in Knowledge Bases</span></a>
</nav>
<!--end of nav bar logo + text -->


<h3 style="text-align: center;">Examples' Sankey diagram: </h3>
<p style="text-align: center; font-size: 30px;"> Generation examples </p>
<div id="sankey_multiple" style="margin: 0 auto;"></div>

<br>
<p style="text-align: center; font-size: 30px;"> Validation examples </p>
<div id="sankey_val_examples" style="margin: 0 auto;"></div>


<script src="https://d3js.org/d3.v3.min.js" charset="utf-8"></script>

<script type="text/javascript">
    //var for generation data
    var mydiag = '${gen_examples}';
    var temp= mydiag.replace(/&quot;/g, '\"').replace(/\&amp;/g,'&').replace(/=&gt;/g, "=>");
    var model = JSON.parse(temp);
    //var for validation data
    var myvaldiag = '${val_examples}';
    var val_temp=myvaldiag.replace(/&quot;/g, '\"').replace(/\&amp;/g,'&').replace(/=&gt;/g, "=>");
    var valmodel = JSON.parse(val_temp);

    google.charts.load("current", {packages:["sankey"]});
    google.charts.setOnLoadCallback(drawChart);
    function drawChart() {
//generation examples
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'From');
        data.addColumn('string', 'To');
        data.addColumn('number', 'Weight');
        data.addRows(model);
//validation examples
        var val_examples = new google.visualization.DataTable();
        val_examples.addColumn('string', 'From');
        val_examples.addColumn('string', 'To');
        val_examples.addColumn('number', 'Weight');
        val_examples.addRows(valmodel);
        // Set chart options
        var dataHeight = data.getNumberOfRows() * 41 + 30;
        var valExHeight=val_examples.getNumberOfRows() * 41 + 30;
        // Set chart options
        var dataOptions = {
            height: dataHeight
        };

        var valDataOptions = {
            height: valExHeight
        };

        // Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.Sankey(document.getElementById('sankey_multiple'));

        chart.draw(data, dataOptions);

// Instantiate and draw our chart, passing in some options.
        var chart = new google.visualization.Sankey(document.getElementById('sankey_val_examples'));
        chart.draw(val_examples, valDataOptions);
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
                    <li> <a href="mailto:Paolo.Papotti@eurecom.fr">Contact</a> </li>
                </ul> </div>
            <!-- Grid column -->


        </div>
        <!-- Grid row -->

    </div>
    <!-- Footer Links -->

</footer>
<!-- Footer -->
</body>
</html>
