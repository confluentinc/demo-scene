module Keyboard exposing
    ( Key(..)
    , parseKey
    )


parseKey : String -> Key
parseKey str =
    case ( str, String.toList str ) of
        ( _, [ char ] ) ->
            Character str

        ( "Delete", _ ) ->
            Delete

        ( "Backspace", _ ) ->
            Delete

        ( "Enter", _ ) ->
            Enter

        ( other, _ ) ->
            Unknown other


type Key
    = Character String
    | Enter
    | Delete
    | Unknown String
