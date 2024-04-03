
**TODO** Document

Build the docker image
```
docker build -t openflexo/server-test .
```


Run the OpenFlexo server
```
docker run -p 8080:8080 openflexo/server-test
```


Access a shell inside the image
```
docker run -it --rm openflexo/server-test /bin/sh
```