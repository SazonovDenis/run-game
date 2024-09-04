class ParserBase:
    nextParser = None

    state = None
    token = None
    tokenType = None

    def handle(self, token, tokenType):
        pass

    def onToken(self, token, tokenType):
        if self.nextParser != None:
            self.nextParser.handle(token, tokenType)


class ParserPrinter(ParserBase):

    def handle(self, token, tokenType):
        if tokenType == "text":
            print(str(token))
        else:
            print(str(tokenType) + ": " + str(token))
