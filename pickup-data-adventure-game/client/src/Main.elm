module Main exposing (main)

import Browser
import State
import Types exposing (..)
import View


main : Program Flags Model Msg
main =
    Browser.application
        { init = State.init
        , onUrlChange = State.onUrlChange
        , onUrlRequest = State.onUrlRequest
        , subscriptions = State.subscriptions
        , update = State.update
        , view = View.view
        }
