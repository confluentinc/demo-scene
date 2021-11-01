module Types exposing (..)

import Array exposing (Array)
import Json.Decode as Json
import Set exposing (Set)
import WebsocketSupport exposing (ResponseEvent)


type alias Flags =
    { userId : String }


type alias Model =
    { userId : String
    , hiddenSources : Set String
    , history : Array HistoryMessage
    , buffer : String
    }


type Msg
    = NoOp
    | WebsocketMessage ServerMessage
    | KeyboardMessage String
    | ToggleHiddenSource String


type HistoryMessage
    = ClientMessage String
    | ServerMessage ServerMessage


type ServerMessage
    = Response ResponseEvent
    | DecodingError Json.Error
    | WebsocketOpened
    | WebsocketClosed
