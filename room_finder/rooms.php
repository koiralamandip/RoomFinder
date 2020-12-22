<?php
require_once 'databaseConnection.php';

if (isset($_GET['REQ_FETCH'])){
  if (isset($_GET['ROOM_ID'])){

  }else{
    $fetch_failed = false;
    $logMsg = "";
    $roomsArray = array();
    $result = $pdo->prepare("SELECT * FROM rooms");
    $result->execute();

    if ($result->rowCount() > 0){
      $rooms = $result->fetchAll();
      //Check password
      foreach ($rooms as $room) {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE user_id = :user_id");
        $criteria = ['user_id' => $room['user_id']];
        $stmt->execute($criteria);
        $user = $stmt->fetch();

        $roomArray = [
          'room_id' => $room['room_id'],
          'latitude' => $room['latitude'],
          'longitude' => $room['longitude'],
          'room_type' => $room['room_type'],
          'price' => $room['price'],
          'room_count' => $room['room_count'],
          'location' => $room['location'],
          'contact' => $room['contact'],
          'owner' => $room['ownerName'],
          'poster' => $user['username'],
          'description' => $room['description'],
          'user_id' => $room['user_id']
        ];
        array_push($roomsArray, $roomArray);
      }
      $fetch_failed = false;
      $logMsg = "SUCCESS";
    }else{
      $fetch_failed = true;
      $logMsg = "No rooms available";
    }

    if ($fetch_failed){
      $obj = [
        'status' => $logMsg
      ];
      echo json_encode($obj);
    }else{
      $obj = [
        'status' => $logMsg,
        'rooms' => $roomsArray
      ];
      echo json_encode($obj);
    }

  }
}
else if (isset($_GET['REQ_ADD'])){
  $add_failed = false;
  $logMsg = "";

  $stmt = $pdo->prepare("INSERT INTO rooms (room_count, room_type, price, location, latitude, longitude, contact, ownerName, description, user_id)
  VALUES (:room_count, :room_type, :price, :location, :latitude, :longitude, :contact, :ownerName, :description, :user_id)");
  $room = $_POST;
  $criteria = [
    'latitude' => ($room['latitude']),
    'longitude' => ($room['longitude']),
    'room_type' => ($room['room_type']),
    'price' => ($room['price']),
    'room_count' =>((int) $room['room_count']),
    'location' => ($room['location']),
    'contact' => ($room['contact']),
    'ownerName' => ($room['ownerName']),
    'description' => ($room['description']),
    'user_id' => ((int)$room['user_id'])
  ];

  $stmt->execute($criteria);

  if ($stmt->rowCount() > 0){
    $logMsg = "SUCCESS";
    $obj = [
      'status' => $logMsg
    ];
    echo json_encode($obj);
  }else{
    $obj = [
      'status' => "Couldn't add room"
    ];
    echo json_encode($obj);
  }

}else if (isset($_GET['REQ_EDIT'])){
  $edit_failed = false;
  $logMsg = "";

  $stmt = $pdo->prepare("UPDATE rooms SET room_count = :room_count, room_type = :room_type, price = :price, location = :location, latitude = :latitude,
    longitude = :longitude, contact = :contact, ownerName = :ownerName, description = :description WHERE room_id = :room_id");
  $room = $_POST;
  $criteria = [
    'latitude' => ($room['latitude']),
    'longitude' => ($room['longitude']),
    'room_type' => ($room['room_type']),
    'price' => ($room['price']),
    'room_count' =>((int) $room['room_count']),
    'location' => ($room['location']),
    'contact' => ($room['contact']),
    'ownerName' => ($room['ownerName']),
    'description' => ($room['description']),
    'room_id' => ((int)$room['room_id'])
  ];

  $stmt->execute($criteria);

  if ($stmt->rowCount() > 0){
    $logMsg = "SUCCESS";
    $obj = [
      'status' => $logMsg
    ];
    echo json_encode($obj);
  }else{
    $obj = [
      'status' => "Couldn't update room"
    ];
    echo json_encode($obj);
  }

}else if (isset($_GET['REQ_DELETE'])){
  $delete_failed = false;
  $logMsg = "";

  $stmt = $pdo->prepare("DELETE FROM rooms WHERE room_id = :room_id");
  $criteria = [
    'room_id' => ((int)$_POST['room_id'])
  ];

  $stmt->execute($criteria);

  if ($stmt->rowCount() > 0){
    $logMsg = "SUCCESS";
    $obj = [
      'status' => $logMsg
    ];
    echo json_encode($obj);
  }else{
    $obj = [
      'status' => "Couldn't delete room"
    ];
    echo json_encode($obj);
  }
}
