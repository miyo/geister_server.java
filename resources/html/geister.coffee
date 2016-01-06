
class GeisterObj
        @COLOR_NONE: 0
        @COLOR_RED: 1
        @COLOR_BLUE: 2
        @PLAYER_NONE: 0
        @PLAYER_A: 1
        @PLAYER_B: 2

        color: null
        player: null
        hidden : false
        moving : false
        
        constructor: (@color, @player) ->

class CellState
        obj: null
        constructor: (@row, @column) ->
                if 2 <= @column <= 5
                        if @row == 1
                                @obj = new GeisterObj(GeisterObj.COLOR_RED, GeisterObj.PLAYER_A)
                        else if @row == 6
                                @obj = new GeisterObj(GeisterObj.COLOR_RED, GeisterObj.PLAYER_B)
                        else if @row == 2
                                @obj = new GeisterObj(GeisterObj.COLOR_BLUE, GeisterObj.PLAYER_A)
                        else if @row == 5
                                @obj = new GeisterObj(GeisterObj.COLOR_BLUE, GeisterObj.PLAYER_B)
                        else
                                @obj = new GeisterObj(GeisterObj.COLOR_NONE, GeisterObj.PLAYER_NONE)
                else
                        @obj = new GeisterObj(GeisterObj.COLOR_NONE, GeisterObj.PLAYER_NONE)

class Player
        got_obj: null

        constructor: ->
                @got_obj = []

        got: (o) ->
                @got_obj.push(o)

class GameOfGeister
        cellSize: 60
        numberOfRows: 8
        numberOfColumns: 8
        canvas: null
        drawingContext: null
        currentCell : null
        arrow_img : null
        geister_obj_img : null
        geister_red_img : null
        geister_blue_img : null
        selectedX : null
        selectedY : null
        selectedItem : null
        turn : 'B'
        game_status : 'PREPARE'
        playerA : null
        playerB : null

        constructor: ->
                @game_status = 'PREPARE'
                @createCanvas()
                @resizeCanvas()
                @createDrawingContext()

                $('#start').click =>
                        @readyGame()
                $('#gameboard').mousedown (e) =>
                        @mouseDown(e)
                
                @ready_resources()

        ready_resources: (f) ->
                @arrow_img = new Image();
                @arrow_img.src = "arrow.png?" + new Date().getTime()
                @arrow_img.onload = =>
                        @geister_obj_img = new Image();
                        @geister_obj_img.src = "geister_obj.png?" + new Date().getTime()
                        @geister_obj_img.onload = =>
                                @geister_red_img = new Image();
                                @geister_red_img.src = "geister_red.png?" + new Date().getTime()
                                @geister_red_img.onload = =>
                                        @geister_blue_img = new Image();
                                        @geister_blue_img.src = "geister_blue.png?" + new Date().getTime()
                                        @geister_blue_img.onload = =>
                                                @initGame()

        initGame: ->
                @playerA = new Player()
                @playerB = new Player()
                @game_status = 'PREPARE'
                @initCells()
                @drawBoard()




        swapOwnObj: (n) ->
                myCells = []
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                c = @currentCell[row][column]
                                myCells.push(c) if c.obj.player == GeisterObj.PLAYER_A
                for i in [0...n]
                        c0 = myCells[parseInt(Math.random() * myCells.length)]
                        c1 = myCells[parseInt(Math.random() * myCells.length)]
                        @swapObject(c0.column, c0.row, c1.column, c1.row)

        readyGame: ->
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                if @currentCell[row][column].obj.player == GeisterObj.PLAYER_A
                                        @currentCell[row][column].obj.hidden = true
                @drawBoard()
                @swapOwnObj(100)
                @game_status = 'RUN'
                start = $('#start')[0].disabled = true

        isEscapeWin: ->
                return GeisterObj.PLAYER_A if @currentCell[6][0].obj.player == GeisterObj.PLAYER_A
                return GeisterObj.PLAYER_A if @currentCell[7][1].obj.player == GeisterObj.PLAYER_A
                return GeisterObj.PLAYER_A if @currentCell[6][7].obj.player == GeisterObj.PLAYER_A
                return GeisterObj.PLAYER_A if @currentCell[7][6].obj.player == GeisterObj.PLAYER_A
                return GeisterObj.PLAYER_B if @currentCell[0][1].obj.player == GeisterObj.PLAYER_B
                return GeisterObj.PLAYER_B if @currentCell[1][0].obj.player == GeisterObj.PLAYER_B
                return GeisterObj.PLAYER_B if @currentCell[0][6].obj.player == GeisterObj.PLAYER_B
                return GeisterObj.PLAYER_B if @currentCell[1][7].obj.player == GeisterObj.PLAYER_B
                return GeisterObj.PLAYER_NONE

        countObjColor: ->
                a = [0, 0, 0, 0] # A.RED, A.BLUE, B.RED, B.BLUE
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                o = @currentCell[row][column].obj
                                if o.player == GeisterObj.PLAYER_A
                                        if o.color == GeisterObj.COLOR_RED
                                                a[0] = a[0] + 1
                                        else
                                                a[1] = a[1] + 1
                                else if o.player == GeisterObj.PLAYER_B
                                        if o.color == GeisterObj.COLOR_RED
                                                a[2] = a[2] + 1
                                        else
                                                a[3] = a[3] + 1
                return a

        nextGame: ->
                p = @checkGame()
                if p == GeisterObj.PLAYER_NONE
                        if @turn == 'B'
                                @turn = 'A'
                                @simplePlayerOp()
                        else
                                @turn = 'B'
                else
                        @drawBoard()
                        if p == GeisterObj.PLAYER_A
                                alert('CPU Win!!')
                        else
                                alert('You Win!!')
                        @initGame()

        checkGame: ->
                winner = @isEscapeWin()
                return winner if winner != GeisterObj.PLAYER_NONE
                colors = @countObjColor()
                return GeisterObj.PLAYER_A if colors[0] == 0 # A.RED  == 0
                return GeisterObj.PLAYER_B if colors[1] == 0 # A.BLUE == 0
                return GeisterObj.PLAYER_B if colors[2] == 0 # B.RED  == 0
                return GeisterObj.PLAYER_A if colors[3] == 0 # B.BLUE == 0
                return GeisterObj.PLAYER_NONE
                
        isEscapeMove: (obj, x, y, dx, dy) ->
#                console.log(player)
                if obj.player == GeisterObj.PLAYER_B and obj.color == GeisterObj.COLOR_BLUE
                        return true if (x == 1 and y == 1 and dx == -1 and dy == 0)
                        return true if (x == 1 and y == 1 and dx == 0 and dy == -1)
                        return true if (x == 6 and y == 1 and dx == 1 and dy == 0)
                        return true if (x == 6 and y == 1 and dx == 0 and dy == -1)
                        return false
                else if obj.player == GeisterObj.PLAYER_A and obj.color == GeisterObj.COLOR_BLUE
                        return true if (x == 1 and y == 6 and dx == -1 and dy == 0)
                        return true if (x == 1 and y == 6 and dx == 0 and dy == 1)
                        return true if (x == 6 and y == 6 and dx == 1 and dy == 0)
                        return true if (x == 6 and y == 6 and dx == 0 and dy == 1)
                        return false
                else
                        return false

        isIllegalMove: (x, y, dx, dy) ->
                o = @currentCell[y][x]
                return true if @isEscapeMove(o.obj, x, y, dx, dy)
                return false if !((dx == 0 and Math.abs(dy) == 1) or (Math.abs(dx) == 1 and dy == 0))
                return false if !(0 < y + dy < @numberOfRows - 1)
                return false if !(0 < x + dx < @numberOfColumns - 1)
                t = @currentCell[y+dy][x+dx]
                return false if t.obj.player == o.obj.player
                return true

        simplePlayerOp: ->
                objects = []
                candidates = []
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                if @currentCell[row][column].obj.player == GeisterObj.PLAYER_A
                                        objects.push([row, column])
                for o in objects
                        candidates.push([o[1], o[0],  0,  1]) if @isIllegalMove(o[1], o[0],  0,  1)
                        candidates.push([o[1], o[0],  0, -1]) if @isIllegalMove(o[1], o[0],  0, -1)
                        candidates.push([o[1], o[0],  1,  0]) if @isIllegalMove(o[1], o[0],  1,  0)
                        candidates.push([o[1], o[0], -1,  0]) if @isIllegalMove(o[1], o[0], -1,  0)

#                for c in candidates
#                        console.log(c)

                # select
                c = candidates[parseInt(Math.random() * candidates.length)]
#                console.log("select:")
#                console.log(c)
                result = @moveOperation(c[0], c[1], c[0] + c[2], c[1] + c[3])
                @drawBoard()
                @nextGame()

        getXPosition: (v) ->
                return parseInt(v / @cellSize)

        getYPosition: (v) ->
                return parseInt(v / @cellSize)

        clearMouseEvent: ->
                if @selectedItem != null
                        @selectedItem.moving = false
                @selectedX = null
                @selectedY = null
                @selectedItem = null
                @drawBoard()
                $('#gameboard').unbind("mouseup mousemove mouseleave")
                $(this).unbind("mouseup mousemove mouseleave")

        swapObject: (sx, sy, ex, ey) ->
                so = @currentCell[sy][sx].obj
                eo = @currentCell[ey][ex].obj
                if so.player == eo.player
                        p_tmp = so.player
                        c_tmp = so.color
                        so.player = eo.player
                        so.color = eo.color
                        eo.player = p_tmp
                        eo.color = c_tmp

        moveOperation: (sx, sy, ex, ey) ->
                console.log("move: " + sx + "," + sy + "->" + ex + "," + ey);
                @selectedItem.moving = false
                so = @currentCell[sy][sx].obj
                eo = @currentCell[ey][ex].obj
                result = new GeisterObj(eo.color, eo.player) # new obj
                if @isIllegalMove(sx, sy, ex-sx, ey-sy) == true
                        p_tmp = so.player
                        c_tmp = so.color
                        h_tmp = so.hidden
                        so.player = GeisterObj.PLAYER_NONE
                        so.color = GeisterObj.COLOR_NONE
                        so.hidden = false
                        eo.player = p_tmp
                        eo.color = c_tmp
                        eo.hidden = h_tmp
                        return result
#                        console.log("set:" + ex + "," + ey + "<-" + p_tmp + "," + c_tmp);
#                        console.log(@currentCell[ex][ey])
                else
                        return false

        mouseUp: (e) ->
                ex = @getXPosition(e.offsetX)
                ey = @getYPosition(e.offsetY)
                sx = @getXPosition(@selectedX)
                sy = @getYPosition(@selectedY)
                if @game_status == 'READY'
                        @swapObject(sx, sy, ex, ey)
                if @game_status == 'RUN' and @turn == 'B'
                        if @isIllegalMove(sx, sy, ex-sx, ey-sy)
                                result = @moveOperation(sx, sy, ex, ey)
                                @playerB.got(result) if result.player != GeisterObj.PLAYER_NONE
                                @nextGame()
                @clearMouseEvent()

        mouseLeave: (e) ->
                @clearMouseEvent()

        mouseMove: (e) ->
                if @selectedItem != null
                        @drawBoard()
                        img = @getObjImage(@selectedItem)
                        @drawingContext.drawImage(img, e.offsetX-@cellSize/2, e.offsetY-@cellSize/2, @cellSize, @cellSize)
                
        mouseDown: (e) ->
                if !(@game_status == 'READY' or (@game_status == 'RUN' and @turn == 'B'))
                        return
                column = @getXPosition(e.offsetX)
                row = @getYPosition(e.offsetY)
                if @currentCell[row][column].obj.player == GeisterObj.PLAYER_B
                        @selectedItem = @currentCell[row][column].obj
                        @selectedX = e.offsetX
                        @selectedY = e.offsetY
                        @selectedItem.moving = true
                        $('#gameboard').mousemove (e) =>
                                @mouseMove(e)
                        $('#gameboard').mouseleave (e) =>
                                @mouseLeave(e)
                        $('#gameboard').mouseup (e) =>
                                @mouseUp(e)

        createCanvas: ->
                @canvas = $('#gameboard')[0]

        resizeCanvas: ->
                @canvas.height = @cellSize * @numberOfRows
                @canvas.width = @cellSize * @numberOfColumns

        createDrawingContext: ->
                @drawingContext = @canvas.getContext '2d'

        initCells: ->
                @currentCell = []
                for row in [0...@numberOfRows]
                        @currentCell[row] = []
                        for column in [0...@numberOfColumns]
                                @currentCell[row][column] = new CellState(row, column)

        drawImage: (img, x, y, rot) ->
                if rot
                        @drawingContext.save()
                        @drawingContext.rotate(180 * Math.PI / 180)
                        x = (-x-1) * @cellSize
                        y = (-y-1) * @cellSize
                else
                        x = x * @cellSize
                        y = y * @cellSize
                @drawingContext.drawImage(img, x, y, @cellSize, @cellSize)
                if rot
                        @drawingContext.restore()

        drawBoard: ->
                @drawingContext.clearRect(0, 0, @drawingContext.canvas.clientWidth, @drawingContext.canvas.clientHeight)
                @drawImage(@arrow_img, 1, 1, false)
                @drawImage(@arrow_img, 6, 1, true)
                @drawImage(@arrow_img, 1, 6, false)
                @drawImage(@arrow_img, 6, 6, true)
                @drawGrid()
#                console.log(@playerB.got_obj)
                for o,i in @playerB.got_obj
                        @drawImage(@getObjImage(o), i, 7, true)
                        
                if @game_status == 'PREPARE'
                        @game_status = 'READY'
                        rdy = $('#start')[0].disabled = false

        drawGrid: ->
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                @drawCell(@currentCell[row][column])

        getObjImage: (obj) ->
                if obj.hidden == true and $('#debug').is(':checked') == false
                        img = @geister_obj_img
                else if obj.color == GeisterObj.COLOR_BLUE
                        img = @geister_blue_img
                else
                        img = @geister_red_img
                return img

        drawCell: (cell) ->
                x = cell.column * @cellSize
                y = cell.row * @cellSize

                if cell.obj.player != GeisterObj.PLAYER_NONE and cell.obj.moving == false
                        img = @getObjImage(cell.obj)
                        @drawImage(img, cell.column, cell.row, cell.obj.player == GeisterObj.PLAYER_A)
                if 0 < cell.column < 7 and 0 < cell.row < 7
                        strk = 'rgba(0, 0, 0, 1)'
                else
                        strk = 'rgba(0, 0, 0, 0)'
                @drawingContext.strokeStyle = strk
                @drawingContext.strokeRect x, y, @cellSize, @cellSize

window.GameOfGeister = GameOfGeister
