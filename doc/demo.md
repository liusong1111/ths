curl -i -X GET   -H "Content-Type:application/json" http://127.0.0.1:3000/demo.json
curl -i -X POST  -H "Content-Type:application/json" http://127.0.0.1:3000/demo.json -d "{\"name\":\"sliu\"}"
curl -i -X PUT   -H "Content-Type:application/json" http://127.0.0.1:3000/demo.json -d "{\"name\":\"sliu\"}"
curl -i -X DELETE -H "Content-Type:application/json" http://127.0.0.1:3000/demo.json
