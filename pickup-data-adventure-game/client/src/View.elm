module View exposing (view)

import Array exposing (Array)
import Browser exposing (Document)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Html.Keyed as Keyed
import Set exposing (Set)
import Types exposing (..)


view : Model -> Document Msg
view model =
    { title = "PICKUP DATA"
    , body =
        [ root model ]
    }


root : Model -> Html Msg
root { history, buffer, hiddenSources } =
    div
        [ class "container"
        ]
        [ header [] [ h1 [] [ text "PICKUP DATA ; GO NORTH" ] ]
        , div [ class "main" ]
            [ historyView hiddenSources history
            , historyMessageView (ClientMessage (buffer ++ "_"))
            ]
        , div [ class "controls" ]
            [ filterControls history hiddenSources ]
        ]


historyView : Set String -> Array HistoryMessage -> Html i
historyView hiddenSources history =
    let
        filteredHistory =
            history
                |> Array.filter
                    (\message ->
                        case extractSource message of
                            Nothing ->
                                True

                            Just source ->
                                not (Set.member source hiddenSources)
                    )
                |> Array.toList
    in
    div [] (List.map historyMessageView filteredHistory)


historyMessageView : HistoryMessage -> Html i
historyMessageView historyMsg =
    case historyMsg of
        ClientMessage message ->
            div []
                [ text ">"
                , nbsp
                , text message
                ]

        ServerMessage (Response message) ->
            case ( message.source, message.value ) of
                ( "WebSocket", _ ) ->
                    empty

                ( "Echo", "ECHO: START" ) ->
                    empty

                _ ->
                    div [ class "server-response" ]
                        [ small [] [ text message.source ]
                        , div [] [ text message.value ]
                        ]

        ServerMessage WebsocketOpened ->
            div []
                [ text "-*- CONNECTED -*-" ]

        ServerMessage WebsocketClosed ->
            div []
                [ text "-*- DISCONNECTED -*-" ]

        ServerMessage (DecodingError error) ->
            div []
                [ text (Debug.toString error) ]


nbsp : Html msg
nbsp =
    text "\u{00A0}"


filterControls : Array HistoryMessage -> Set String -> Html Msg
filterControls messages hiddenSources =
    let
        allSources : Set String
        allSources =
            messages
                |> Array.toList
                |> List.filterMap extractSource
                |> Set.fromList
    in
    div []
        [ h3 [] [ text "Filter By Source" ]
        , Keyed.node "div"
            []
            (allSources
                |> Set.toList
                |> List.sort
                |> List.map (filterButton hiddenSources)
            )
        ]


filterButton : Set String -> String -> ( String, Html Msg )
filterButton hiddenSources sourceName =
    let
        active =
            Set.member sourceName hiddenSources
    in
    ( sourceName
    , button
        [ classList
            [ ( "uk-button", True )
            , ( "uk-button-small", True )
            , ( "uk-button-default", active )
            , ( "uk-button-primary", not active )
            ]
        , onClick (ToggleHiddenSource sourceName)
        ]
        [ text sourceName ]
    )


extractSource : HistoryMessage -> Maybe String
extractSource msg =
    case msg of
        ServerMessage (Response { source }) ->
            Just source

        _ ->
            Nothing


empty : Html i
empty =
    span [] []
