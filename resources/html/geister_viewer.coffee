
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
                @obj = new GeisterObj(GeisterObj.COLOR_NONE, GeisterObj.PLAYER_NONE)

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
                @ws = new WebSocket('ws://localhost:8080/ws/geister');
                @createCanvas()
                @resizeCanvas()
                @createDrawingContext()
                @ready_resources()
                @ws.geister = this
                @ws.onmessage = (e) -> @geister.update_info(e)

        str2color: (s) ->
                s = s.toUpperCase()
                return GeisterObj.COLOR_RED if s == 'R'
                return GeisterObj.COLOR_BLUE if s == 'B'
                return GeisterObj.COLOR_NONE

        update_info: (e) ->
                msg = e.data
                $('#message').text(msg)
                @currentCell = []
                for row in [0...@numberOfRows]
                        @currentCell[row] = []
                        for column in [0...@numberOfColumns]
                                @currentCell[row][column] = new CellState(row, column)
                for i in [0...16]
                        item = msg[3*i..3*i+2]
                        x = parseInt(item[0])
                        y = parseInt(item[1])
                        c = item[2]
                        if x < 6 and y < 6
                                if i < 8
                                        @currentCell[y+1][x+1].obj.player = GeisterObj.PLAYER_B
                                else
                                        @currentCell[y+1][x+1].obj.player = GeisterObj.PLAYER_A
                                @currentCell[y+1][x+1].obj.color = @str2color(c)
                @drawBoard()

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
                @initCells()
                @drawBoard()

        readyGame: ->
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                if @currentCell[row][column].obj.player == GeisterObj.PLAYER_A
                                        @currentCell[row][column].obj.hidden = true
                @drawBoard()
                @swapOwnObj(100)
                @game_status = 'RUN'
                start = $('#start')[0].disabled = true

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

        drawGrid: ->
                for row in [0...@numberOfRows]
                        for column in [0...@numberOfColumns]
                                @drawCell(@currentCell[row][column])

        getObjImage: (obj) ->
                return @geister_obj_img if $('#hidden').is(':checked') == true
                return @geister_blue_img if obj.color == GeisterObj.COLOR_BLUE
                return @geister_red_img if obj.color == GeisterObj.COLOR_RED
                return @geister_obj_img

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

console.log("start")
window.GameOfGeister = GameOfGeister