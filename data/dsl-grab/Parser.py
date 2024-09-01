class ParserBase:
    onToken = None

    state = None
    token = None
    tokenType = None

    def next(self, token, tokenType):
        pass


class ParserPrinter(ParserBase):

    def next(self, token, tokenType):
        if tokenType == "text":
            print(str(token))
        else:
            print(str(tokenType) + ": " + str(token))
