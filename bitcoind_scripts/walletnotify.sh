#!/bin/bash
if [ ${#1} -lt 1 ]
then
  exit -1
fi

# We know that the len(txid) >= 1
echo "[$(date)] walletnotify.sh: $1" >> /home/main/notify.log

RESPONSE=$(curl "http://localhost:9000/callback/txnotify/$1")
echo "   Result: $RESPONSE" >> /home/main/notify.log
