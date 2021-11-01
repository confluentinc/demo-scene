module State exposing
    ( init
    , onUrlChange
    , onUrlRequest
    , subscriptions
    , update
    )

import Array
import Browser exposing (..)
import Browser.Events exposing (onKeyUp)
import Browser.Navigation as Nav
import Char
import Json.Decode as Decode
import Json.Encode as Encode
import Keyboard exposing (parseKey,Key(..))
import Set
import Set.Extras as Set
import String
import Types exposing (..)
import Url exposing (Url)
import WebsocketSupport


init : Flags -> Url -> Nav.Key -> ( Model, Cmd Msg )
init { userId } _ _ =
    ( { userId = userId
      , history = Array.empty
      , hiddenSources =
            Set.fromList
                [ "Echo"
                , "Websocket"
                , "Position"
                ]
      , buffer = ""
      }
    , Cmd.none
    )


onUrlChange : Url -> Msg
onUrlChange _ =
    NoOp


onUrlRequest : UrlRequest -> Msg
onUrlRequest _ =
    NoOp


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.batch
        [ WebsocketSupport.onMessage
            (\msg ->
                (case Decode.decodeString WebsocketSupport.decodeResponseEvent msg of
                    Err err ->
                        DecodingError err

                    Ok value ->
                        Response value
                )
                    |> WebsocketMessage
            )
        , WebsocketSupport.onOpen (always (WebsocketMessage WebsocketOpened))
        , WebsocketSupport.onClose (always (WebsocketMessage WebsocketClosed))
        , onKeyUp
            (Decode.field "key" Decode.string
                |> Decode.map (Debug.log "KEY is")
                |> Decode.map KeyboardMessage
            )
        ]


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )

        WebsocketMessage serverMsg ->
            ( { model
                | history = Array.push (ServerMessage serverMsg) model.history
              }
            , Cmd.none
            )

        KeyboardMessage key ->
            case parseKey key of
                Enter ->
                    ( { model
                        | buffer = ""
                        , history = Array.push (ClientMessage model.buffer) model.history
                      }
                    , WebsocketSupport.sendMessageToServer
                        { userId = model.userId
                        , msg = model.buffer
                        }
                    )

                Delete ->
                    ( { model | buffer = String.dropRight 1 model.buffer }
                    , Cmd.none
                    )

                Character other ->
                    ( { model | buffer = model.buffer ++ String.toUpper other }
                    , Cmd.none
                    )

                Unknown _ ->
                    ( model, Cmd.none )

        ToggleHiddenSource sourceId ->
            ( { model
                | hiddenSources =
                    Set.toggle sourceId model.hiddenSources
              }
            , Cmd.none
            )
