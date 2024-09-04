from Parser import ParserBase


class ParserStr(ParserBase):

    def handle(self, token, tokenType):
        if token == "\n":
            self.onToken("", "new-line")
        else:
            for s in token:
                if s == "\n":
                    self.onToken("", "end-line")
                else:
                    self.onToken(s, "text")

