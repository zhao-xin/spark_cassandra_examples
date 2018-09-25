<?php 
error_reporting(-1);
ini_set('display_errors', 'On');
echo "Hello World (php)\n"; 
?>
<?php
$cluster   = Cassandra::cluster()
 ->withContactPoints('172.23.99.231')
               ->withPort(9042)                 // connects to localhost by default
                 ->build();
$keyspace  = 'cloudcomputing';
$session   = $cluster->connect($keyspace);        // create session, optionally scoped to a keyspace
$statement = new Cassandra\SimpleStatement(       // also supports prepared and batch statements
    'SELECT * FROM data'
);
$future    = $session->executeAsync($statement);  // fully asynchronous and easy parallel execution
$result    = $future->get();                      // wait for the result, with an optional timeout

foreach ($result as $row) {                       // results and rows implement Iterator, Countable and ArrayAccess
    printf("%s\n", implode("|",$row));
}
?>
