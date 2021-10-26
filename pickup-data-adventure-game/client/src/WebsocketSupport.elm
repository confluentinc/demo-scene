port module WebsocketSupport exposing
    ( ClientMessage
    , ResponseEvent
    , decodeResponseEvent
    , encodeClientMessage
    , onClose
    , onMessage
    , sendToServer
    )

import Json.Decode as Json exposing (Decoder, succeed)
import Json.Decode.Pipeline exposing (required)
import Json.Encode as Encode


port sendToServer : String -> Cmd msg


port onMessage : (String -> msg) -> Sub msg


port onClose : (() -> msg) -> Sub msg


type alias ResponseEvent =
    { source : String
    , key : String
    , value : String
    }


decodeResponseEvent : Decoder ResponseEvent
decodeResponseEvent =
    succeed ResponseEvent
        |> required "source" Json.string
        |> required "key" Json.string
        |> required "value" Json.string


type alias ClientMessage =
    { key : String
    , value : String
    }


encodeClientMessage : ClientMessage -> Encode.Value
encodeClientMessage { key, value } =
    Encode.object
        [ ( "key", Encode.string key )
        , ( "value", Encode.string value )
        ]
