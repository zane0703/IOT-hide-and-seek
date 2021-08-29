from flask import Flask,redirect,render_template,request
from flask_socketio import SocketIO 
import signal
# import RPi.GPIO as GPIO 
# import I2C_LCD_driver

from threading import Thread
import os
from time import sleep,time
import math
from logging import error, fatal, getLogger
from requests import get as getRequest
myThread=None
app = Flask(__name__)
log = getLogger('werkzeug')
log.disabled = True
app.logger.disabled = True
socket_ = SocketIO(app,async_mode=None)
def exitProgram(a,b):
    socket_.emit("gameEnd",{"message":"Game end due to server close"},namespace="/")
    sleep(1)
    exit()
signal.signal(signal.SIGINT,exitProgram)
# GPIO.setup(27,GPIO.OUT)
# GPIO.setmode(GPIO.BCM) # for seeker in range
# GPIO.setwarnings(False)
# GPIO.setup(22,GPIO.IN)
# GPIO.setup(24,GPIO.OUT)
# GPIO.setup(18,GPIO.OUT)
# pwm = GPIO.PWM(24,50)
#set var
# switchStatus =GPIO.input(22)
# LCD = I2C_LCD_driver.lcd()
seekerLocation=None
hiderCount =0
hiderCount2=0
startTime=0.0
isGameStarted=False
hadSeeker =False
while True :
    try:
        minPWDDutyCycle = float(input("Enter min LED PWM distance (metre) :"))
        break
    except ValueError:
        print("Invalid input")
while True:
    try:
        minDistance = float(input("Enter min hider distance (metre) :"))
        if minDistance<minPWDDutyCycle:
            print("hider distance cannot be smaller then PWN distance")
        else:
            break
    except ValueError:
        print("Invalid input")
earthRadius=  6367000.0
# LCD.lcd_clear()
# MATRIX=[ ['1','2','3'],
#          ['4','5','6'],
#          ['7','8','9'],
#          ['*','0','#']]
# ROW=[6,20,19,13]
# COL=[12,5,16]
# for i in COL:
#     GPIO.setup(i,GPIO.OUT)
#     GPIO.output(i,1)
# for j in ROW:
#     GPIO.setup(j,GPIO.IN,pull_up_down=GPIO.PUD_UP)
# def getKeyInput():
#     while (True):
#         for i in range(3): #loop thru’ all columns
#             GPIO.output(COL[i],0) #pull one column pin low
#             for j in range(4): #check which row pin becomes low
#                 if GPIO.input(ROW[j])==0: #if a key is pressed
#                     while GPIO.input(ROW[j])==0: #debounce
#                         sleep(0.1)
#                     return MATRIX[j][i]
#             GPIO.output(COL[i],1) #write back default value of 1
# LCD.lcd_display_string("Enter min LED PWM distance (metre) :", 1) 
# minPWDDutyCycle =''
# keyInputIndex=1
# while True :
#     keyInput = getKeyInput()
#     if keyInput == '*':
#         if keyInputIndex>1:
#             minPWDDutyCycle = minPWDDutyCycle[:-1]
#             LCD.lcd_display_string(" ", 2,keyInputIndex)
#             keyInputIndex-=1 
#     elif keyInput=='#':
#         LCD.lcd_clear()
#         minPWDDutyCycle = float(minPWDDutyCycle)
#         break
#     else:
#         minPWDDutyCycle += keyInput
#         LCD.lcd_display_string(keyInput, 2,keyInputIndex)
#         keyInputIndex+=1
# LCD.lcd_display_string("Enter min hider distance (metre) :", 1) 
# minDistance=''
# keyInputIndex=1
# while True :
#     keyInput = getKeyInput()
#     if keyInput == '*':
#         if keyInputIndex>1:
#             minDistance = minDistance[:-1]
#             LCD.lcd_display_string(" ", 2,keyInputIndex)
#             keyInputIndex-=1         
#     elif keyInput=='#':
#         LCD.lcd_clear()
#         minDistance = float(minDistance)
#         if  minDistance<minPWDDutyCycle:
#             LCD.lcd_display_string("hider distance cannot be smaller then PWN distance", 1)
#             sleep(2)
#             LCD.lcd_clear()
#             LCD.lcd_display_string("Enter min hider distance (metre) :", 1)
#             minDistance=''
#         else:
#             break
#     else:
#         minDistance += keyInput
#         LCD.lcd_display_string(keyInput, 2,keyInputIndex)
#         keyInputIndex+=1

# for i in COL:
#     GPIO.output(i,0)
DutyCycleOffset= 100/minPWDDutyCycle
class Hider:
    def __init__(this):
        global hiderCount
        this.id =hiderCount=hiderCount+1
        this.found=False
    def setLocation(this,latitude , longitude ):
        print(this.id)
        this.latitude = math.radians(latitude)
        this.longitude = math.radians(longitude)
    def setFound(this,found):
        this.found=found
    def getDistance(this,latitude , longitude):
        lat2 = math.radians(latitude)
        lon2 = math.radians(longitude)
        dlon = lon2 - this.longitude
        dlat = lat2 - this.latitude
        a = math.sin(dlat / 2)**2 + math.cos(this.latitude) * math.cos(lat2) * math.sin(dlon / 2)**2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        return earthRadius * c
hiders = {}
def showTime():
    # LCD.lcd_display_string("Time", 2)
    global switchStatus
    global startTime
    global isGameStarted
    startTime = time()
    while isGameStarted:
        # if GPIO.input(22) !=switchStatus:
        #     endGame("Game ended by a switch")
        currentTime = (time() - startTime)
        # LCD.lcd_display_string(str(math.floor(currentTime/60))+":"+"{:02d}".format(math.floor(currentTime % 60)), 2,5) 
        print(math.floor(currentTime/60),":","{:02d}".format(math.floor(currentTime % 60)))
        sleep(1)
@app.route("/",methods=["GET"])
def getIndex():
    return  render_template("index.html")
@app.route("/hider",methods=["GET"])
def getHiderPage():
    global isGameStarted
    if isGameStarted:
       return redirect("/?err=Game%20already%20%20started")
    else:
        global hiders
        hider=Hider()
        hiders[hider.id] =hider
        return render_template("hider.html",id=hider.id)
@app.route("/seeker",methods=["GET"])
def getSeekerPage():
    global hadSeeker
    if hadSeeker:
        return redirect("/?err=There%20already%20had%20seeker")
    else:
        hadSeeker = True
        return render_template("seeker.html")
#api
@app.route("/api/hider",methods=["POST"])
def addHider():
    global isGameStarted
    if isGameStarted:
       return ("",409)
    else:
        global hiders
        hider=Hider()
        hiders[hider.id] =hider
        return (str(hider.id),201)

@app.route("/test")
def test():
    return bytes([65])
@app.route("/api/hider/<id>/location",methods=["PUT"])
def setHiderLocation(id):
    global hiders
    global hiderCount
    global hiderCount2
    global seekerLocation
    try:
        hider = hiders[int(id)]
        hiderCount2 -= 1
        hider.setLocation(request.json["latitude"],request.json["longitude"])
        if hider.getDistance(seekerLocation["latitude"],seekerLocation["longitude"]) >minDistance :
            return ("",422)
        if hiderCount2 < 1:
            seekerLocation = None
            socket_.emit("allFound",namespace="/")
        return ('', 204)
    except ValueError:
        return ('',400)
@app.route("/api/hider/<id>",methods=["DELETE"])
def hiderCaught(id):
    try:
        del(hiders[int(id)])
        if(request.json["caught"]):
            def run():
                print("buzz")
                # GPIO.output(18,1)
                # sleep(1)
                # GPIO.output(18,0)
            Thread(target=run).run()
        if len(hiders) ==0:
            endGame("all the hider have been caught")
        return ("",204)
    except ValueError:
        return ("",400)
@app.route("/api/seeker",methods=["POST"])
def addSeekerApi():
    global hadSeeker
    if hadSeeker:
        return ("",403)
    else:
        hadSeeker = True
        return ("",201)
@app.route("/api/seeker",methods=["DELETE"])
def deleteSeeker():
    global hadSeeker
    hadSeeker=False
    return ("",204)

@app.route("/api/seeker/start",methods=["POST"])
def startGame():
    global isGameStarted
    global hiderCount2
    global hiderCount
    global seekerLocation
    global switchStatus
    # switchStatus =GPIO.input(22)
    seekerLocation =request.json 
    hiderCount2= hiderCount
    isGameStarted =True
    try:
        Thread(target=showTime).start()
    except error:
        print(error)
    socket_.emit("start",{"start":True},namespace="/")
    return ('', 204)
@app.route("/api/seeker/end",methods=["GET"])
def seekerEndGame():
    endGame("The game have ended by the seeker")
    return ("",204)
def endGame(message):
    global hadSeeker
    global isGameStarted
    global hiderCount
    global hiders
    hiders = {}
    hadSeeker=False
    isGameStarted =False
    hiderCount =0
    socket_.emit("gameEnd",{"message":message},namespace="/")
    #public view https://thingspeak.com/channels/1298817
    getRequest("http://api.thingspeak.com/update?api_key=GMP3BM3QMDM0H71Z&field1=%s"%(time()-startTime))
#socket
@socket_.on('connect', namespace='/')
def socket_connect():
    pass
@socket_.on("seekerPosition",namespace="/")
def setSeekerPosition(message):
    distance=[]
    for key,value in hiders.items():
        distance.append(value.getDistance(message["latitude"],message["longitude"]))
    minHiderDistance= min(distance)
    duty_cycle=0.0
    if minHiderDistance<minPWDDutyCycle:
        duty_cycle=100.0-minHiderDistance*DutyCycleOffset
    print("duty_cycle=",duty_cycle)
    print("Distance=",minHiderDistance)
    # pwm.start(duty_cycle)
    if minHiderDistance< 10:
        print("hider in range")
        #LCD.lcd_display_string("hider in range     ", 1) 
        #GPIO.output(27,1)
    else:
        print("hider not in range")
        #LCD.lcd_display_string("hider not in range", 1) 
        #GPIO.output(27,0)
if __name__=="__main__":
    os.environ["WERKZEUG_RUN_MAIN"] = 'true'
    myThread =  Thread(target=lambda a:a.run(host="0.0.0.0",port=5001),args=[app])
    myThread.daemon = True
    myThread.start()
    app.run(host="0.0.0.0",port=5000,ssl_context=('localhost.crt', 'localhost.key'))#,ssl_context=('localhost.crt', 'localhost.key'))