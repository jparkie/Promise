# Promise
[![Build Status](https://travis-ci.org/jparkie/Promise.svg?branch=master)](https://travis-ci.org/jparkie/Promise)

A lightweight promise library for Java 6+ for method-count restricted environments.

Inspired by https://github.com/linkedin/parseq.

- ~32 KB jar.
- Zero dependencies.
- Java 6+ & Android 2.3+.
- Non-opinionated schedulers.
- Async or synchronous execution.
- Cancellation as a first-class concept.
- `firstCompletedOf()`, `whenAll()`, `zip()`.

## Downloads

**Maven**
````
TODO.
````

**Gradle**
````
TODO.
````

## Optional Classes

As a lightweight promise library, the following packages can be omitted:
- [com.github.jparkie.promise.actions](https://github.com/jparkie/Promise/tree/master/src/main/java/com/github/jparkie/promise/actions)
- [com.github.jparkie.promise.extras](https://github.com/jparkie/Promise/tree/master/src/main/java/com/github/jparkie/promise/extras)
- [com.github.jparkie.promise.functions](https://github.com/jparkie/Promise/tree/master/src/main/java/com/github/jparkie/promise/functions)

## Usages

Refer to https://github.com/jparkie/Promise/tree/master/src/test/java/com/github/jparkie/promise for more.

### Creating Promises
````java
final Promise<String> promise = Promises.promise();
final Promise<String> valuePromise = Promises.value("Hello World.");
final Promise<String> errorPromise = Promises.error(new NoSuchElementException());
````

### Resolving Promises
````java
final Promise<String> promise = Promises.promise();
// Method 1:
promise.set("Hello World.");
// Method 2:
promise.setError(new NoSuchElementException());
````
### Awaiting on Promises
````java
final Promise<String> promise = Promises.promise();

promise.set("Hello World.");

try {
    promise.await();
    // Console Output: Hello World.
    System.out.println(promise.get());
} catch (InterruptedException e) {
    e.printStackTrace();
}
````

### Listening to Promises
````java
final Promise<String> promise = Promises.promise();
promise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
    @Override
    public void call(Promise<String> promise) {
        if (promise.isSuccessful()) {
            // Console Output: Hello World.
            System.out.println(promise.get());
        } else {
            promise.getError().printStackTrace();
        }
    }

    @Override
    public void cancel() {
        System.out.println("Cancelled.");
    }
});

promise.set("Hello World.");
````

### Transforming Promises
````java
final Promise<String> promise = Promises.promise();
final Promise<String> transformedPromise = promise
        .then(Schedulers.newSimpleScheduler(), new Function<String, String>() {
            @Override
            public Promise<String> call(Promise<String> promise) {
                return Promises.value("Transform 1.");
            }
        })
        .then(Schedulers.newSimpleScheduler(), new Function<String, String>() {
            @Override
            public Promise<String> call(Promise<String> promise) {
                return Promises.value("Transform 2.");
            }
        })
        .then(Schedulers.newSimpleScheduler(), new Function<String, String>() {
            @Override
            public Promise<String> call(Promise<String> promise) {
                return Promises.value("Transform 3.");
            }
        });

promise.set("Hello World.");

try {
    transformedPromise.await();
    // Console Output: Transform 3.
    System.out.println(promise.get());
} catch (InterruptedException e) {
    e.printStackTrace();
}
````

### Cancelling Promises
````java
final Promise<String> promise = Promises.promise();
promise.then(Schedulers.newSimpleScheduler(), new Action<String>() {
    @Override
    public void call(Promise<String> promise) {
        if (promise.isSuccessful()) {
            System.out.println(promise.get());
        } else {
            promise.getError().printStackTrace();
        }
    }

    @Override
    public void cancel() {
        System.out.println("Cancelled.");
    }
});

promise.cancel();

// The then() Action<String> is never called.
````

## Build

````bash
$ git clone https://github.com/jparkie/Promise.git
$ cd Promise/
$ ./gradlew build
````
