<?php
require_once 'databaseConnection.php';

if (isset($_GET['REQ_LOGIN'])){
  $loginFailed = false;
  $logMsg = "";
  $user_data = null;

  $result = $pdo->prepare("SELECT * FROM users WHERE username = :username");
  $criteria = [
    'username' => $_POST['username']
  ];
  $result->execute($criteria);
  //Check username
  if ($result->rowCount() > 0){
    $user = $result->fetch();
    //Check password
    if (password_verify($_POST['password'], $user['password'])){
      $logMsg = "SUCCESS";
      $loginFailed = false;
      $user_data = $user;
    }else{
      $loginFailed = true;
      $logMsg = 'Your password is incorrect';
    }
  }else{
    $loginFailed = true;
    $logMsg = 'Your username is incorrect';
  }

  if ($loginFailed){
    echo json_encode(["status" => $logMsg]);
  }else{
    $obj = [
      "status" => $logMsg,
      "data" => [
        "user_id" => $user_data["user_id"],
        "firstname" => $user_data["firstname"],
        "surname" => $user_data["surname"],
        "phone" => $user_data["phone"],
        "username" => $user_data["username"]
      ]
    ];
    echo json_encode($obj);
  }
}else if (isset($_GET["REQ_JOIN"])){
  $join_failed = false;
  $logMsg = "";
  $checkUser = $pdo->prepare("SELECT * FROM users WHERE username = :username");
  $criteria = ['username' => $_POST['username']];
  $checkUser->execute($criteria);
  if ($checkUser->rowCount() > 0){
    $join_failed = true;
    $logMsg = 'Username already taken';
  }else{
    // If error free, insert the user to database record
    $result = $pdo->prepare("INSERT INTO users (firstname, surname, phone, username, password) VALUES (:firstname, :surname, :phone, :username, :password)");
    $criteria = [
      'firstname' => htmlspecialchars($_POST['firstname']),
      'surname' => htmlspecialchars($_POST['surname']),
      'phone' => htmlspecialchars($_POST['phone']),
      'username' => htmlspecialchars($_POST['username']),
      'password' => password_hash($_POST['password'], PASSWORD_DEFAULT),
    ];
    $result->execute($criteria);
    //Display logs
    if ($result){
      $join_failed = false;
      $logMsg = "SUCCESS";

      // $image = $_POST['image'];
      $folderPath = "images/profiles/";
      $image = str_replace('data:image/jpeg;base64,', '', $_POST['image']);
      $image = str_replace(' ', '+', $image);
      // Decode the Base64 encoded Image
    $data = base64_decode($image);
    $file = $folderPath . $_POST['username'] . $pdo->lastInsertId() . '.jpg';
    file_put_contents($file, $data);
    }else{
      $join_failed = true;
      $logMsg = 'Registration unsuccessful. Please try again!';
    }
  }

  $obj = [
    "status" => $logMsg
  ];

  echo json_encode($obj);
}

?>
