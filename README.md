erlang code:

```erlang
    run() ->
      Number = receive
        I when is_integer(I) ->
          I
        S when is_list(S) ->
          {N, _Rest} = string:to_integer(S),
          N
      end,
      io:format("got int: ~p", [Number]),
      Other = receive
        X -> X
      end,
      io:format("got other: ~p", [Other]).

    Pid = spawn(run)
```

erla code:

```scala
    val actor = new Actor with ErlActor {
      def actX() = {
        val number = reactX {
          case i: Int => i
          case s: String => s.toInt
        }
        println("got int: %s" format number)
        val other = reactX {
          case x => x
        }
        println("got other: %s" format other)
      }
    }.start()
```

erla(akka2 version) code:

```scala
    val actor = system.actorOf(Props(new ErlActor {
      def act() = {
        val number = react {
          case i: Int => i
          case s: String => s.toInt
        }
        println("got int: %s" format number)
        val other = react {
          case x => x
        }
        println("got other: %s" format other)
      }
    }))
```