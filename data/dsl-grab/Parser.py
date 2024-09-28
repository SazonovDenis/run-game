class ParserBase:
    nextParser = None
    errorCollector = None

    state = None
    token = None
    tokenType = None

    def handle(self, token, tokenType):
        pass

    def onToken(self, token, tokenType):
        if self.nextParser != None:
            self.nextParser.handle(token, tokenType)

    def onError(self, token, error):
        if self.errorCollector != None:
            self.errorCollector.handleError(token, error)
        else:
            print("ERROR: " + error)
            print("token.text: " + token["text"])
            print()


class ParserPrinter(ParserBase):

    def handle(self, token, tokenType):
        if tokenType == "text":
            print(str(token))
        else:
            print(str(tokenType) + ": " + str(token))
