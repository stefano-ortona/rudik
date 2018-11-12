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

    <!-- CDN for dataTable -->
    <link rel="stylesheet" href=" https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css ">
    <link rel="stylesheet" href=" https://cdn.datatables.net/1.10.19/css/dataTables.bootstrap.min.css ">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/dt-1.10.18/datatables.min.css"/>
    <script type="text/javascript" src="https://cdn.datatables.net/v/dt/dt-1.10.18/datatables.min.js"></script>

    <link rel=" icon" type="image/png" href="assets/images/RuDiK_icon32.png">
    <title>RuDiK - Rules discovery in Knowledge bases</title>

    <!-- loader when submitting the forms rudik -->
    <script>
        function showLoader(){
            $("#overlay").toggle();
        }
    </script>

    <!-- Hide #overlay when clicking browser back button so that the loader disappears -->
    <script type="text/javascript">
        function reloadOnPreviousPageButton() {
            if (performance.navigation.type == 2) {
                $("#overlay").style.display='none';
            }
        }
    </script>
</head>

<style>
    html { height: 100%; }

    table {
        font-family: arial, sans-serif;
        border-collapse: collapse;
        width: 100%;
        margin-left:auto;
        margin-right:auto;
    }

    td, th {
        border: 1px solid #dddddd;
        text-align: left;
        padding: 8px;
    }

    tr:nth-child(even) {
        background-color: #dddddd;
    }
    td:hover{
        cursor: pointer;
        background-color: #687eff;
    }
     body {
        position: relative;
        margin: 0;
        min-height: 100%;
       }
    footer {
        right: 0;
        left: 0;
        }
	
	.page_wrap{
               min-height: 100%;
               margin-bottom: 44%;
              }

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


<body>

<!--nav bar logo + text -->
<nav class="navbar navbar-dark bg-dark">
    <img src="assets/images/Logo_RuDiK.png" width="12%" height="12%">
     <a class="rudik_header" href="/rudik" style="font-family: 'Segoe UI'; font-size: 40px;text-align: center; padding-right: 20%;"><span style="color: #00aeff;">RuDiK: Rule</span> <span style="color: #90fc3b;"> Discovery</span><span style="color: #ff0b0b;"> in Knowledge Bases</span> </a>
</nav>
<!--end of nav bar logo + text -->

<!--loader -->
<div class="overlay" id="overlay">
    <div class="loader" id="loader"></div>
</div>

<!-- Table to show the rules -->

<!-- output's table -->
<div class="container">
    <div class="page_wrap">



<table id="rules" class="table table-striped table-bordered" style="width:100%">
    <#list output>
<thead>
        <tr>
        <th style="text-align: center; font-size: 30px;">Output rules (${output?size})
	     <form action="discover-rules/sankey-diagram">
		<button type="submit" class="btn btn-primary" style="float: right; margin-right: 20px;">Sankey diagram</button>
              </form>
   </th>
    </tr>
</thead>
        <tbody>
   <#items as rule>
    <tr>
        <td>
        <!--<form action="instantiate-rule/">
                  <input type="hidden" name="kg" value=${kg}>
                  <input type="hidden" name="rule" value=${rule}> -->
                  <div class="form-group row" style="margin-left: 15px;">
                    <div class="col-sm-12">
                      ${rule}
                       <form action="/instantiate-rule" onclick="showLoader()">
			           <input type="hidden" name="instKg" value="${kg}">
                       <input type="hidden" name="rule" value="${rule}">
                       <input type="hidden" name="instEndpoint" value="${endpoint}">
                      <button type="submit" class="btn btn-primary" style="float: right;">Instantiate</button>
                    
		</form>
		 <form action="discover-rules/examples">
                       <input type="hidden" name="instantiatedRule" value="${rule}">
                      <button type="submit" class="btn btn-primary" style="float: right; margin-right:20px">Examples</button>

                </form>

                 </div>
                 </div>
        
</td>
    </tr>
    </#items>
        </tbody>
</table>
	<#else>
             <table>
               <tr>
        <th style="text-align: center; font-size: 30px;">Output rules</th>
               </tr>
	       <tr>
                 <td>Sorry, no new rule discovered...Please try again!</td>
    	       </tr>
	     </table>
</#list>

        <br>
        <br>

    </div>
</div>

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

<!--script for dataTable -->
<script type="text/javascript">
    $(document).ready(function() {
        $('#rules').DataTable();
    } );
</script>

</body>

</html>


