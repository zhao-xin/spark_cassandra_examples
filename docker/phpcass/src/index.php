<?php
    // enable php debug
    error_reporting(-1);
    ini_set('display_errors', 'On');
?>

<html>
    <head>
        <title>Cloud computing Prac 8</title>
    </head>
    <body>
        <h1>
            <?php
                echo "sensor records of St Lucia\n";
            ?>
        </h1>

        <?php
            $cluster = Cassandra::cluster()
                ->withContactPoints('172.17.0.3','172.17.0.4') // cassandra address 
                ->withPort(9042)
                ->build();
            $keyspace = 'cloudcomputing'; // keyspace
            $session = $cluster->connect($keyspace);
            $statement = new Cassandra\SimpleStatement(
                "SELECT * FROM data WHERE campus = 'St Lucia'" // cql sentence
            );
            $future = $session->executeAsync($statement); // fully asynchronous and easy parallel execution
            $result = $future->get(); // wait for the result, with an optional timeout

            echo "<table>";
            foreach ($result as $row) { // results and rows implement Iterator, Countable and ArrayAccess
                    echo "<tr><td>" . implode("|",$row)  .  "</td></tr>";
            }
            echo "</table>";
        ?>
    </body>
</html>
