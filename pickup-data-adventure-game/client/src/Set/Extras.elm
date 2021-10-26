module Set.Extras exposing (toggle)

import Set exposing (..)


toggle :
    comparable
    -> Set comparable
    -> Set comparable
toggle item set =
    if member item set then
        remove item set

    else
        insert item set
