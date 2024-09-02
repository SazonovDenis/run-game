from enum import IntEnum

from Parser import ParserBase


class ParserLex(ParserBase):
    class State(IntEnum):
        WAIT_CHAR = 1
        WAIT_MASKED_CHAR = 2
        WAIT_RIGHT_BRACKET = 3

    def __init__(self):
        self.state = self.State.WAIT_CHAR
        self.token = None
        self.tokenType = None

    def collectToken(self, text):
        if (self.token == None):
            self.token = ""
            self.tokenType = "text"
        self.token = self.token + text

    def flushToken(self):
        if self.token != None:
            self.onToken.next(self.token, self.tokenType)
            self.token = None
            self.tokenType = None

    def next(self, token, tokenType):
        # прокидываем не обрабатываемый токен выше
        if tokenType == "new-line":
            self.onToken.next(token, tokenType)
            #
            return

        # обрабатываемые токены
        if tokenType == "end-line":
            self.flushToken()
            #
            return

        if self.state == self.State.WAIT_MASKED_CHAR:
            #
            self.state = self.State.WAIT_CHAR
            self.collectToken(token)
            #
            return

        if self.state == self.State.WAIT_CHAR:
            if token == "[":
                self.flushToken()
                #
                self.state = self.State.WAIT_RIGHT_BRACKET
                self.token = ""
                self.tokenType = "tag"
                #
                return

            elif token == "\\":
                self.state = self.State.WAIT_MASKED_CHAR
                #
                return

            elif token == "♢":
                self.tokenType = "tag"
                self.token = "diamond"
                self.flushToken()
                #
                self.state = self.State.WAIT_CHAR
                #
                return

        if self.state == self.State.WAIT_RIGHT_BRACKET:
            if token == "]":
                self.flushToken()
                #
                self.state = self.State.WAIT_CHAR
                #
                return

            elif token == "/":
                self.tokenType = "tag-close"
                #
                return

        #
        self.collectToken(token)
